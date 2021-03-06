package com.ivan.passbook.merchants.vo;

import com.ivan.passbook.merchants.constant.ErrorCode;
import com.ivan.passbook.merchants.dao.MerchantsDao;
import com.ivan.passbook.merchants.entity.Merchants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * vo :在业务之间传递的对象 value object
 * <h2>创建商户请求对象</h2>
 * @Author Ivan 14:08
 * @Description 商户入驻
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class CreateMerchantsRequest {

    /** 商户名称 */
    private String name;

    /** 商户 logo */
    private String logoUrl;

    /** 商户营业执照*/
    private String businessLicenseUrl;

    /** 商户联系电话 */
    private String phone;

    /** 商户地址 */
    private String address;

    /**
     * <h2> 验证请求的有效性 </h2>
     * @param merchantsDao
     * @return {@link ErrorCode}
     */
    public ErrorCode validate(MerchantsDao merchantsDao){

        if(this.name == null || "".equals(this.name)){
            return ErrorCode.MERCHANTS_NAME_ERROR;
        }

        if (merchantsDao.findByName(this.name)!=null){
            return ErrorCode.DUPLICATE_NAME;
        }

        if (null == logoUrl){
            return ErrorCode.EMPTY_LOGO;
        }

        if (null == businessLicenseUrl){
            return ErrorCode.EMPTY_BUSINESS_LICENESS;
        }

        if (null == address){
            return ErrorCode.EMPTY_ADDRESS;
        }

        if (null == phone){
            return ErrorCode.ERROR_PHONE;
        }

        return ErrorCode.SUCCESS;
    }

    /***
     * <h2>将请求对象转换为商户对象</h2>
     *
     * @return {@link Merchants}
     * 为了方便之后保存到数据库中，jpa的save（）
     */
    public Merchants toMerchants(){

        Merchants merchants = new Merchants();

        merchants.setName(name);
        merchants.setLogoUrl(logoUrl);
        merchants.setAddress(address);
        merchants.setBusinessLicenseUrl(businessLicenseUrl);
        merchants.setPhone(phone);

        return merchants;

    }
}
