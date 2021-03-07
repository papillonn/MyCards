package com.ivan.passbook.merchants.controller;

import com.alibaba.fastjson.JSON;
import com.ivan.passbook.merchants.service.IMerchantsServ;
import com.ivan.passbook.merchants.vo.CreateMerchantsRequest;
import com.ivan.passbook.merchants.vo.PassTemplate;
import com.ivan.passbook.merchants.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

/**
 * @Author Ivan 20:00
 * @Description TODO
 */
@Slf4j
@RestController
@RequestMapping("/merchants")
public class MerchantsController {


    private IMerchantsServ merchantsServ;
    //使用构造函数的方法注入
    @Autowired
    public MerchantsController(IMerchantsServ merchantsServ) {
        this.merchantsServ=merchantsServ;
    }

    @ResponseBody
    @PostMapping("/create")
    public Response createMerchants(@RequestBody CreateMerchantsRequest request){
        //@RequestBody 使前端传来的json对象可以序列化成request对象
        log.info("CreateMerchants : {}", JSON.toJSONString(request));
        return merchantsServ.createMerchants(request);
    }

    @ResponseBody
    @GetMapping("/{id}")
    public Response buildMerchantsById(@PathVariable String id){
        //商户根据自己入驻时返回的id，来查询自己的信息,中文字符暂时不行
        log.info("BuildMerchants :{}",id);
        Pattern pattern = Pattern.compile("[0-9]*");
        if(pattern.matcher(id).matches()){
            return merchantsServ.buildMerchantsById(Integer.parseInt(id));
        }else {
            return merchantsServ.buildMerchantsByName(id);
        }
    }

    @ResponseBody
    @PostMapping("/drop")
    public Response dropPassTemplate(@RequestBody PassTemplate template){
        log.info("DropPassTemplate : {}",template);
        return merchantsServ.dropPassTemplate(template);
    }

}
