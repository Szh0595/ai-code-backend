package com.szh.aicodebackend.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.szh.aicodebackend.model.dto.app.AppQueryRequest;
import com.szh.aicodebackend.model.entity.App;
import com.szh.aicodebackend.model.entity.User;
import com.szh.aicodebackend.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author Lenovo
 */
public interface AppService extends IService<App> {

    /**
     * 获取应用视图对象
     *
     * @param app
     * @return
     */
    public AppVO getAppVO(App  app);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用视图对象列表
     *
     * @param appList
     * @return
     */
    public List<AppVO> getAppVOList(List<App> appList);

    /**
     * 聊天生成代码
     *
     * @param appId
     * @param userMessage
     * @param loginUser
     * @return
     */
    public Flux<String> chatToGenCode(Long appId, String userMessage, User loginUser);

    /**
     * 删除应用同时删除历史记录
     *
     * @param id
     * @return
     */
    boolean removeById(Long id);

    /**
     * 获取精选应用列表(缓存)
     *
     * @return
     */
    Page<AppVO> getGoodAppVOListByCache(AppQueryRequest appQueryRequest);

    String deployApp(Long appId, User loginUser);
}
