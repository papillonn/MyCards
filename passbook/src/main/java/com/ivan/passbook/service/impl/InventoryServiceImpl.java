package com.ivan.passbook.service.impl;

import com.ivan.passbook.constant.Constants;
import com.ivan.passbook.dao.MerchantsDao;
import com.ivan.passbook.entity.Merchants;
import com.ivan.passbook.mapper.PassTemplateRowMapper;
import com.ivan.passbook.service.IInventoryService;
import com.ivan.passbook.service.IUserPassService;
import com.ivan.passbook.utils.RowKeyGenUtil;
import com.ivan.passbook.vo.*;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.LongComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1>获取库存信息，只返回用户没有领取的优惠券</h1>
 * @Author Ivan 17:09
 * @Description TODO
 */
@Slf4j
@Service
public class InventoryServiceImpl implements IInventoryService {

    private final HbaseTemplate hbaseTemplate;

    private final MerchantsDao merchantsDao;


    private final IUserPassService userPassService;

    @Autowired
    public InventoryServiceImpl(HbaseTemplate hbaseTemplate, MerchantsDao merchantsDao, IUserPassService userPassService) {
        this.hbaseTemplate = hbaseTemplate;
        this.merchantsDao = merchantsDao;
        this.userPassService = userPassService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response getIventoryInfo(Long userId) throws Exception {

        Response allUserPass = userPassService.getUserAllPassInfo(userId);
        //object强转
        List<PassInfo> passInfos = (List<PassInfo>) allUserPass.getData();

        List<PassTemplate> excludeObject = passInfos.stream().map(
                PassInfo::getPassTemplate
        ).collect(Collectors.toList());
        List<String> excludeIds = new ArrayList<>();

        //根据优惠券模板生成需要排除的id
        excludeObject.forEach(e->excludeIds.add(
                RowKeyGenUtil.genPassTemplateRowKey(e)
        ));


        return new Response(new InventoryResponse(userId,
                buildPassTemplateInfo(getAvailablePassTemplate(excludeIds))));
    }

    /**
     * <h2>获取系统中可用的优惠券</h2>
     * @param excludeIds 需要排除的优惠券 ids（用户已经领取的优惠券）
     * @return
     */
    private List<PassTemplate> getAvailablePassTemplate(List<String> excludeIds){

        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);

        //limit>0或
        filterList.addFilter(
                new SingleColumnValueFilter(
                        Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C),
                        Bytes.toBytes(Constants.PassTemplateTable.LIMIT),
                        CompareFilter.CompareOp.GREATER,
                        new LongComparator(0L)
                )
        );
        //或者优惠券不存在上限
        filterList.addFilter(
                new SingleColumnValueFilter(
                        Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C),
                        Bytes.toBytes(Constants.PassTemplateTable.LIMIT),
                        CompareFilter.CompareOp.EQUAL,
                        Bytes.toBytes("-1")
                )
        );

        Scan scan = new Scan();
        scan.setFilter(filterList);

        List<PassTemplate> validTemplates = hbaseTemplate.find(
                Constants.PassTemplateTable.TABLE_NAME, scan, new PassTemplateRowMapper());
        List<PassTemplate> availableTemplates = new ArrayList<>();

        Date cur = new Date();

        //过滤掉用户已经有的优惠券
        for (PassTemplate validTemplate:validTemplates){
            if (excludeIds.contains(RowKeyGenUtil.genPassTemplateRowKey(validTemplate))){
                continue;
            }

            //过期的也不要
            if (cur.getTime()>=validTemplate.getStart().getTime()
                && cur.getTime()<=validTemplate.getEnd().getTime()){
                availableTemplates.add(validTemplate);
            }
        }
        return availableTemplates;

    }

    /**
     * <h2>构造优惠券的信息</h2>
     * @param passTemplates
     * @return
     */
    private List<PassTemplateInfo> buildPassTemplateInfo(List<PassTemplate> passTemplates){

        Map<Integer,Merchants> merchantsMap = new HashMap<>();
        List<Integer> merchantsIds = passTemplates.stream().map(
                PassTemplate::getId
        ).collect(Collectors.toList());
        List<Merchants> merchants = merchantsDao.findByIdIn(merchantsIds);
        //merchants 变成 merchantsMap，下面查询用
        merchants.forEach(m->merchantsMap.put(m.getId(),m));

        List<PassTemplateInfo> result = new ArrayList<>();

        for (PassTemplate passTemplate:passTemplates){
            Merchants mc = merchantsMap.getOrDefault(passTemplate.getId(), null);
            if (mc == null){
                log.error("Merchants Error:{}",passTemplate.getId());
                continue;
            }

            result.add(new PassTemplateInfo(passTemplate,mc));
        }
        return result;

    }
}
