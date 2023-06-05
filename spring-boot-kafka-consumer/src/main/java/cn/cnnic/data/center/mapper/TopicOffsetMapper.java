package cn.cnnic.data.center.mapper;

import cn.cnnic.data.center.dto.OffsetDto;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface TopicOffsetMapper extends Mapper<OffsetDto> {
}
