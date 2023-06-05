package cn.cnnic.data.center.service;

import cn.cnnic.data.center.dto.OffsetDto;
import cn.cnnic.data.center.dto.OrderDto;
import cn.cnnic.data.center.mapper.OrderMapper;
import cn.cnnic.data.center.mapper.TopicOffsetMapper;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 将 kafka offset跟业务
 * 一个事务保存
 */
@Service
public class TopicOffsetService {
    @Autowired
    public TopicOffsetMapper mapper;

    @Autowired
    public OrderMapper orderMapper;

    @Transactional(rollbackFor = Exception.class)
    public void save(OffsetDto dto, OrderDto order) {

        mapper.insertSelective(dto);
       // System.out.println(1/0);
        orderMapper.insertSelective(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(OffsetDto dto, OrderDto order) {
        List<OffsetDto> list = list(dto.getTopic(), dto.getPartitions());
        if (list != null) {
            OffsetDto  dd= list.get(0);
            dd.setOffset(dto.getOffset());
            dd.setUpdateTime(LocalDateTime.now());

            mapper.updateByPrimaryKeySelective(dd);
            // System.out.println(1/0);
            orderMapper.insertSelective(order);
        }
    }

    public List<OffsetDto> list(String topic, Integer partNum){
        Example example = new Example(OffsetDto.class);
        Example.Criteria criteria = example.createCriteria();

        if (StringUtils.isNotBlank(topic)) {
            //criteria.andLike("topic", "%" + topic + "%");
            criteria.andEqualTo("topic", topic);
        }
        if (partNum != null) {
            criteria.andEqualTo("partitions", partNum);
        }
        example.setOrderByClause("update_time desc");
        PageHelper.startPage(1, 1);

        return mapper.selectByExample(example);
    }


    public Long getPartitionByTopic(String topic, Integer partNum){
        Long pp = 0L;
        Example example = new Example(OffsetDto.class);
        Example.Criteria criteria = example.createCriteria();

        if (StringUtils.isNotBlank(topic)) {
            //criteria.andLike("topic", "%" + topic + "%");
            criteria.andEqualTo("topic", topic);
        }
        if (partNum != null) {
            criteria.andEqualTo("partitions", partNum);
        }
        example.setOrderByClause("update_time desc");
        PageHelper.startPage(1, 1);

        List<OffsetDto> list =  mapper.selectByExample(example);
        if ( list != null && list.size() > 0) {
            pp = list.get(0).getOffset();
        }

        return pp;
    }

}
