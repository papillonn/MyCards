package com.ivan.passbook.service.impl;

import com.alibaba.fastjson.JSON;
import com.ivan.passbook.constant.Constants;
import com.ivan.passbook.mapper.PassTemplateRowMapper;
import com.ivan.passbook.service.IGainPassTemplateService;
import com.ivan.passbook.utils.RowKeyGenUtil;
import com.ivan.passbook.vo.GainPassTemplateRequest;
import com.ivan.passbook.vo.PassTemplate;
import com.ivan.passbook.vo.Response;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <h1>用户领取优惠券功能实现</h1>
 * @Author Ivan 20:25
 * @Description TODO
 */
@Slf4j
@Service
public class GainPassTemplateServiceImpl implements IGainPassTemplateService {

    private final HbaseTemplate hbaseTemplate;

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public GainPassTemplateServiceImpl(HbaseTemplate hbaseTemplate, StringRedisTemplate redisTemplate) {
        this.hbaseTemplate = hbaseTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Response gainPassTemplate(GainPassTemplateRequest request) throws Exception {

        // 从HBase 获取一个优惠券对象
        PassTemplate passTemplate;

        String passTemplateId = RowKeyGenUtil.genPassTemplateRowKey(
                request.getPassTemplate());

        try {
            passTemplate = hbaseTemplate.get(
                    Constants.PassTemplateTable.TABLE_NAME,
                    passTemplateId,
                    new PassTemplateRowMapper()
            );
        }catch (Exception ex){
            log.error("Gain PassTemplate Error:{}", JSON.toJSONString(request.getPassTemplate()));
            return Response.failure("Gain PassTemplate Error!");
        }
        //  当前优惠券是否能被领取
        //      -- 是否超过限制（limit）
        //      -- 是否过期（start，end）
        if (passTemplate.getLimit()<=1&& passTemplate.getLimit()!=-1){
            log.error("PassTemplate Limit Max: {}",
                    JSON.toJSONString(request.getPassTemplate()));
            return Response.failure("PassTemplate Limit Max!");
        }

        Date cur = new Date();
        if (!(cur.getTime()>=passTemplate.getStart().getTime()
        &&cur.getTime()<=passTemplate.getEnd().getTime())){
            log.error("PassTemplate ValidTime Error: {}",
                    JSON.toJSONString(request.getPassTemplate()));
            return Response.failure("PassTemplate ValidTime Error!");
        }

        //感觉下面两个应该写成一个事务
        // 减去优惠券的 limit
        if (passTemplate.getLimit()!=-1){
            List<Mutation> datas = new ArrayList<>();
            byte[] FAMILY_C = Constants.PassTemplateTable.FAMILY_C.getBytes();
            byte[] LIMIT = Constants.PassTemplateTable.LIMIT.getBytes();

            Put put = new Put(Bytes.toBytes(passTemplateId));
            put.addColumn(FAMILY_C, LIMIT,
                    Bytes.toBytes(passTemplate.getLimit() - 1));
            datas.add(put);

            hbaseTemplate.saveOrUpdates(Constants.PassTemplateTable.TABLE_NAME,
                    datas);
        }

        // 将优惠券保存到用户优惠券表
        if (!addPassForUser(request,passTemplate.getId(),passTemplateId)){
            return Response.failure("GainPassTemplate Failure!");
        }

        return Response.success();
    }

    /**
     * <h2>给用户添加优惠券</h2>
     * @param request
     * @param merchantId
     * @param passTemplateId
     * @return
     * @throws Exception
     */
    private boolean addPassForUser(GainPassTemplateRequest request,
                                   Integer merchantId,
                                   String passTemplateId)throws Exception{

        byte[] FAMILY_I = Constants.PassTable.FAMILY_I.getBytes();
        byte[] USER_ID = Constants.PassTable.USER_ID.getBytes();
        byte[] TEMPLATE_ID = Constants.PassTable.TEMPLATE_ID.getBytes();
        byte[] TOKEN = Constants.PassTable.TOKEN.getBytes();
        byte[] ASSIGNED_DATE = Constants.PassTable.ASSIGNED_DATE.getBytes();
        byte[] CON_DATE = Constants.PassTable.CON_DATE.getBytes();

        List<Mutation> datas = new ArrayList<>();
        Put put = new Put(Bytes.toBytes(RowKeyGenUtil.genPassRowKey(request)));
        put.addColumn(FAMILY_I,USER_ID,Bytes.toBytes(request.getUserId()));
        put.addColumn(FAMILY_I,TEMPLATE_ID,Bytes.toBytes(passTemplateId));

        if (request.getPassTemplate().getHasToken()){
            String token = redisTemplate.opsForSet().pop(passTemplateId);
            if (null == token){
                log.error("Token not exist:{}",passTemplateId);
                return false;
            }
            //把这个token放在用户已经领过的token的文件里
            recordTokenToFile(merchantId,passTemplateId,token);
            put.addColumn(FAMILY_I,TOKEN,Bytes.toBytes(token));
        }else {
            //token不存在
            put.addColumn(FAMILY_I,TOKEN,Bytes.toBytes("-1"));
        }

        put.addColumn(FAMILY_I,ASSIGNED_DATE,Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT.format(new Date())));
        //消费日期写-1
        put.addColumn(FAMILY_I,CON_DATE,Bytes.toBytes("-1"));

        datas.add(put);

        hbaseTemplate.saveOrUpdates(Constants.PassTable.TABLE_NAME,datas);

        return true;
    }

    /**
     * <h2>将已使用的 token 记录到文件中 </h2>
     * @param merchantsId 商户 id
     * @param passTemplateId 优惠券 id
     * @param token 分配的优惠券 token
     */
    private void recordTokenToFile(Integer merchantsId,String passTemplateId,
                                   String token) throws IOException {
        Files.write(
                Paths.get(Constants.TOKEN_DIR,String.valueOf(merchantsId),
                        passTemplateId+Constants.USED_TOKEN_SUFFIX),
                (token + "\n").getBytes(),
                StandardOpenOption.CREATE,StandardOpenOption.APPEND
        );
    }
}
