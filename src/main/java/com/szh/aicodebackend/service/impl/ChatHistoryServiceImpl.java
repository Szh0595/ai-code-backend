package com.szh.aicodebackend.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.szh.aicodebackend.model.entity.ChatHistory;
import com.szh.aicodebackend.mapper.ChatHistoryMapper;
import com.szh.aicodebackend.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author Lenovo
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

}
