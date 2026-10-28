package com.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 审计日志工具类
 * 专门用于记录关键业务操作：登录、增删改、状态变更、权限变更等
 * 输出到独立的 audit.log 文件，便于合规审计和安全分析
 */
@Slf4j
public class AuditLogger {

    // 使用专门的 logger，对应 logback.xml 中的 com.reggie.audit
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("com.reggie.audit");

    /**
     * 记录登录审计
     */
    public static void login(String userType, Long userId, String username, String ip, boolean success, String failReason) {
        AUDIT_LOG.info("type=LOGIN userType={} userId={} username={} ip={} success={} reason={}",
                userType, userId, maskUsername(username), ip, success, failReason);
    }

    /**
     * 记录登出审计
     */
    public static void logout(String userType, Long userId, String username) {
        AUDIT_LOG.info("type=LOGOUT userType={} userId={} username={}", userType, userId, maskUsername(username));
    }

    /**
     * 记录新增操作审计
     */
    public static void create(String operatorType, Long operatorId, String entityType, Long entityId, String entityName) {
        AUDIT_LOG.info("type=CREATE operatorType={} operatorId={} entityType={} entityId={} entityName={}",
                operatorType, operatorId, entityType, entityId, entityName);
    }

    /**
     * 记录更新操作审计
     */
    public static void update(String operatorType, Long operatorId, String entityType, Long entityId, String changes) {
        AUDIT_LOG.info("type=UPDATE operatorType={} operatorId={} entityType={} entityId={} changes={}",
                operatorType, operatorId, entityType, entityId, changes);
    }

    /**
     * 记录删除操作审计
     */
    public static void delete(String operatorType, Long operatorId, String entityType, Long entityId, String entityName) {
        AUDIT_LOG.info("type=DELETE operatorType={} operatorId={} entityType={} entityId={} entityName={}",
                operatorType, operatorId, entityType, entityId, entityName);
    }

    /**
     * 记录状态变更审计（启用/禁用、起售/停售等）
     */
    public static void statusChange(String operatorType, Long operatorId, String entityType, Long entityId, int oldStatus, int newStatus) {
        AUDIT_LOG.info("type=STATUS_CHANGE operatorType={} operatorId={} entityType={} entityId={} oldStatus={} newStatus={}",
                operatorType, operatorId, entityType, entityId, oldStatus, newStatus);
    }

    /**
     * 记录权限/角色变更审计
     */
    public static void permissionChange(String operatorType, Long operatorId, String targetType, Long targetId, String changes) {
        AUDIT_LOG.info("type=PERMISSION_CHANGE operatorType={} operatorId={} targetType={} targetId={} changes={}",
                operatorType, operatorId, targetType, targetId, changes);
    }

    /**
     * 记录敏感操作审计（导出、批量操作等）
     */
    public static void sensitiveOperation(String operatorType, Long operatorId, String operation, String detail) {
        AUDIT_LOG.info("type=SENSITIVE_OPERATION operatorType={} operatorId={} operation={} detail={}",
                operatorType, operatorId, operation, detail);
    }

    /**
     * 脱敏用户名/手机号 - 只显示首尾
     */
    private static String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return "***";
        }
        if (username.length() <= 4) {
            return username.charAt(0) + "***" + username.charAt(username.length() - 1);
        }
        return username.substring(0, 2) + "***" + username.substring(username.length() - 2);
    }
}