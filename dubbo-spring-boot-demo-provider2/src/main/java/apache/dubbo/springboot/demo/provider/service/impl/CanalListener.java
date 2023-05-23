package apache.dubbo.springboot.demo.provider.service.impl;

import cn.cnnic.dataCenter.dto.CanalMessage;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Canal + RabbitMQ 监听数据库数据变化
 *
 * @author <a href="mailto:qiangjishen@gmail.com">qiangjishen</a>
 * @date 2023/3/4 23:14
 */
@Component
@Slf4j
public class CanalListener {

    @RabbitHandler
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "canal.queue", durable = "true"),
                    exchange = @Exchange(value = "canal.exchange"),
                    key = "canal.routing.key"
            )
    }, containerFactory="rabbitListenerContainerFactory")
    public void handleDataChange(Message message) {
        String messageBody = new String(message.getBody());

        System.out.println(messageBody);
        JSONObject object = JSONObject.parseObject(messageBody);
        //log.info("Canal监听到数据发生变化\n库名：{}\n表名：{}\n类型：{}\n数据：{}", object.getString("database"), object.getString("table"), object.getString("type"), object.getString("data"));

        CanalMessage canalMessage = JSON.toJavaObject(object,CanalMessage.class);

        String tableName = canalMessage.getTable();

        log.info("Canal监听到数据发生变化\n库名：{}\n表名：{}\n类型：{}\n数据：{}", canalMessage.getDatabase(), canalMessage.getTable(), canalMessage.getType(), canalMessage.getData());
/**
 if ("sys_oauth_client".equals(tableName)) {
 log.info("======== 清除客户端信息缓存 ========");
 oauthClientService.cleanCache();
 } else if (Arrays.asList("sys_permission", "sys_role", "sys_role_permission").contains(tableName)) {
 log.info("======== 刷新角色权限缓存 ========");
 permissionService.refreshPermRolesRules();
 } else if (Arrays.asList("sys_menu", "sys_role", "sys_role_menu").contains(tableName)) {
 log.info("======== 清理菜单路由缓存 ========");
 menuService.cleanCache();
 }
 **/
    }
}
