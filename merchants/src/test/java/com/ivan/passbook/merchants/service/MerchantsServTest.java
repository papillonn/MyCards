package com.ivan.passbook.merchants.service;

import com.alibaba.fastjson.JSON;
import com.ivan.passbook.merchants.vo.CreateMerchantsRequest;
import com.ivan.passbook.merchants.vo.PassTemplate;
import com.ivan.passbook.merchants.vo.Response;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <h1>商户服务测试类</h1>
 * @Author Ivan 14:59
 * @Description TODO
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MerchantsServTest {

    @Autowired
    private IMerchantsServ merchantsServ;
    @Autowired
    private CreateMerchantsRequest request;
    //Autowired he component 可以一起使用

    @Test
    @Transactional
    public void testCreateMerchants(){
    // Transactional注解是事务的意思
        //加 Transactional之后，数据库就加不上数据了，他能识别test这个注解，就会回滚
        request.setName("QQByte");
        request.setLogoUrl("www.baidu.com");
        request.setBusinessLicenseUrl("www.qq.com");
        request.setPhone("13355645684");
        request.setAddress("北京市海淀区");

        Response merchants = merchantsServ.createMerchants(request);

        System.out.println(JSON.toJSONString(merchants));

    }

    @Test
    public void testBuildMerchants(){
        Integer id = 23;
        Response merchants = merchantsServ.buildMerchantsById(id);

        System.out.println(JSON.toJSONString(merchants));

    }

    @Test
    public void testDropPassTemplate(){
        PassTemplate passTemplate = new PassTemplate();
        passTemplate.setId(23);
        passTemplate.setTitle("晚餐券");
        passTemplate.setSummary("简介: SEU剧院cat");
        passTemplate.setDesc("详情：请点击查看");
        passTemplate.setLimit(10000L);
        passTemplate.setHasToken(false);
        passTemplate.setBackground(2);
        passTemplate.setStart(DateUtils.addDays(new Date(),-10));
        passTemplate.setEnd(DateUtils.addDays(new Date(),10));

        Response response = merchantsServ.dropPassTemplate(passTemplate);
        System.out.println(JSON.toJSONString(response));

    }

    @Test
    public void testConsumer(){

        Receiver.listen("1");

    }

}
