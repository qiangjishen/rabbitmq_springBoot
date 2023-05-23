package cn.cnnic.dataCenter.dao;


import cn.cnnic.dataCenter.dto.CustomerDto;
import com.alibaba.fastjson.JSONObject;


import java.util.List;

public interface CustomerDao {


    /**
     * 列表
     */
    List<CustomerDto> list(JSONObject jsonObject);




}
