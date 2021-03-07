package com.ivan.passbook.merchants.vo;

import com.ivan.passbook.merchants.constant.ErrorCode;
import com.ivan.passbook.merchants.dao.MerchantsDao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * <h1>投放的优惠券对象的定义</h1>
 * @Author Ivan 10:54
 * @Description 传递的对象封装在这里
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassTemplate {

    /** 所属商户 id*/
    private Integer id;

    /** 优惠券标题*/
    private String title;

    /** 优惠券摘要*/
    private String summary;

    /** 优惠券的详细信息*/
    private String desc;

    /** 最大个数限制*/
    private Long limit;

    /** 优惠券是否有token，用于商户核销*/
    private Boolean hasToken;   //token 存储在Redis Set中，每次领取从Redis中获取

    /** 优惠券的背景颜色 */
    private Integer background;

    /** 优惠券开始时间*/
    private Date start;

    /** 优惠券结束时间 */
    private Date end;

    /**
     * <h2> 校验优惠券对象的有效性</h2>
     * @param merchantsDao
     * @return
     */
    public ErrorCode validate(MerchantsDao merchantsDao){
        //意思就是，商户发优惠券之前，要在表里注册过了，要是没注册过，就返回商户不存在这个错误。
        if (null == merchantsDao.findById(id)){
            return ErrorCode.MERCHANTS_NOT_EXIST;
        }

        return ErrorCode.SUCCESS;

    }
}
