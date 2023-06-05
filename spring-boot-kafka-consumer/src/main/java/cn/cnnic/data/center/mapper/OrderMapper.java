package cn.cnnic.data.center.mapper;

import cn.cnnic.data.center.dto.OrderDto;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface OrderMapper  extends Mapper<OrderDto> {
}
