package com.szh.aicodebackend.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.szh.aicodebackend.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.szh.aicodebackend.model.entity.ChatHistory;
import com.szh.aicodebackend.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author Lenovo
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话消息
     * @param appId 应用id
     * @param message 消息
     * @param messageType 消息类型
     * @param userId 用户id
     * @return
     */
    public boolean addChatMessage(Long appId, String message,String messageType,Long userId);

    /**
     * 删除对话消息
     * @param id
     * @return
     */
    public boolean deleteById(Long id);

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 获取应用下的对话历史
     * @param appId 应用id
     * @param pageSize 每页大小
     * @param lastCreateTime 最后创建时间
     * @param loginUser 登录用户
     * @return
     */
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser);

    /**
     * 加载应用下的对话历史到内存中
     * @param appId 应用id
     * @param messageWindowChatMemory 会话历史内存
     * @param maxCount 最大数量
     * @return
     */
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory messageWindowChatMemory, int maxCount);
}
