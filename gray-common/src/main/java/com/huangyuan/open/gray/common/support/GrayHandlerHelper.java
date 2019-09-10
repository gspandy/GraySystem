package com.huangyuan.open.gray.common.support;

import com.facishare.eservice.base.result.EserviceResult;
import com.facishare.wechat.gray.api.model.result.GrayApplicationResult;
import com.facishare.wechat.gray.api.service.GrayApplicationHandlerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.net.InetAddress;
import java.util.List;

/**
 * @author huangy on 2018/9/4
 */
@Component
public class GrayHandlerHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrayHandlerHelper.class);

    @Resource
    private GrayApplicationHandlerService grayApplicationHandlerService;

    /**
     * 判断这台机器上、这个服务是否需要灰度，需要的话，返回group
     *  （1）同一个服务，暂不支持绑定多个灰度环境
     *  （2）不同的服务，可以绑定不同灰度环境。比如说A、B两个服务，A可以绑定group1、B可以绑定group2
     * @param applicationName 服务名称
     * @return group的值
     */
    public String justGrayAndGetGroup(String applicationName) {

        // 查询该服务的灰度配置
        EserviceResult<GrayApplicationResult> modelResult = grayApplicationHandlerService.getGrayApplicationConfig(applicationName);

        if (modelResult == null) {
            LOGGER.error("justGary : grayApplicationHandlerService.getGrayApplicationConfig fail, modelResult is null, applicationName={}",
                    applicationName);
            return null;

        } else if (!modelResult.isSuccess() || (modelResult.getData() == null)) {
            LOGGER.warn("justGary : grayApplicationHandlerService.getGrayApplicationConfig fail, modelResult={}, applicationName={}",
                    modelResult, applicationName);
            return null;

        } else {
            GrayApplicationResult result = modelResult.getData();

            // 检查该灰度环境及该应用是否灰度
            if (result.checkGray()) {

                // 获取该服务的灰度机器列表
                List<String> grayIpList = result.getIpAddressList();

                // 获取本机IP
                String ip = getLocalIp();

                // 判断该机器是否需要灰度
                if (justThisIpIsGray(grayIpList, ip)) {
                    return result.getGroupName();
                } else {
//                    LOGGER.info("this ip address is not gray ip, ip={}, applicationName={}", ip, applicationName);
                    return null;
                }

            } else {
                return null;
            }
        }
    }

    /**
     * 获取某个服务的灰度分组
     * @param applicationName 服务名称
     * @return group的值
     */
    public String getGrayGroup(String applicationName) {
        // 查询该服务的灰度配置
        EserviceResult<GrayApplicationResult> modelResult = grayApplicationHandlerService.getGrayApplicationConfig(applicationName);

        if (modelResult == null) {
            LOGGER.error("getGrayGroup : grayApplicationHandlerService.getGrayApplicationConfig fail, modelResult is null, applicationName={}",
                    applicationName);
            return null;

        } else if (!modelResult.isSuccess() || (modelResult.getData() == null)) {
            LOGGER.warn("getGrayGroup : grayApplicationHandlerService.getGrayApplicationConfig fail, modelResult={}, applicationName={}",
                    modelResult, applicationName);
            return null;

        } else {
            GrayApplicationResult result = modelResult.getData();

            return result.getGroupName();
        }
    }

    /**
     * 判断这台机器上、这个服务是否灰度服务
     * @param applicationName 服务名称
     * @return group的值
     */
    public boolean justGaryByIpAndApplication(String applicationName) {
        try {
            // 查询该服务的灰度配置
            EserviceResult<GrayApplicationResult> modelResult = grayApplicationHandlerService.getGrayApplicationConfig(applicationName);

            if (modelResult == null) {
                LOGGER.error("justGaryByIpAndApplication : grayApplicationHandlerService.justGaryByIpAndApplication fail, modelResult is null, applicationName={}",
                        applicationName);
                return false;

            } else if (!modelResult.isSuccess() || (modelResult.getData() == null)) {
                LOGGER.warn("justGaryByIpAndApplication : grayApplicationHandlerService.justGaryByIpAndApplication fail, modelResult={}, applicationName={}",
                        modelResult, applicationName);
                return false;

            } else {
                GrayApplicationResult result = modelResult.getData();

                // 检查该灰度环境及该应用是否灰度
                if (result.checkGray()) {

                    // 获取该服务的灰度机器列表
                    List<String> grayIpList = result.getIpAddressList();

                    // 获取本机IP
                    String ip = getLocalIp();

                    // 判断该机器是否需要灰度
                    if (justThisIpIsGray(grayIpList, ip)) {
                        return true;
                    } else {
//                        LOGGER.info("this ip address is not gray ip, ip={}, applicationName={}", ip, applicationName);
                        return false;
                    }

                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.error("justGaryByIpAndApplication fail, applicationName={}", applicationName, e);
            // 降级，走正常服务
            return false;
        }
    }

    // 获取本机IP
    public static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            LOGGER.error("get local ip fail", e);
            return null;
        }
    }

    private boolean justThisIpIsGray(List<String> grayIpList, String ip) {
        return (grayIpList.contains("all") || grayIpList.contains(ip));
    }
}
