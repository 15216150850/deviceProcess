package com.imes.controller;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: deviceProcess
 * @description: 工作流执行Demo
 * @author: 钟欣凯
 * @create: 2018-09-17 11:21
 **/
@Controller
public class TestController {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    /**
     * 部署流程图
     *
     * @return
     */
    @GetMapping("delopy")
    @ResponseBody
    public String delopy() {
        Deployment saas = repositoryService.createDeployment().addClasspathResource("dia/deviceCheck3.bpmn")
                .addClasspathResource("dia/deviceCheck3.png").name("saas").deploy();
        return "delopy success";
    }

    /**
     * 启动流程图的流程定义
     *
     * @return
     */
    @GetMapping("start")
    @ResponseBody
    public String startProgress() {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceManageIds", "zs,ls,ws");
        map.put("defenderIds", "a,b,c");
        map.put("chargeIds", "c,d,e");
        ProcessInstance deviceCheck3 = runtimeService.startProcessInstanceByKey("deviceCheck3Pro", map);
        return "start success";
    }

    /**
     * 删除一个流程定义
     *
     * @return
     */
    @GetMapping("delete")
    @ResponseBody
    public String deleteProDef() {
        String key = "deviceCheck3Pro";
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list();
        for (ProcessDefinition processDefinition :
                list) {
            String id = processDefinition.getDeploymentId();
            repositoryService.deleteDeployment(id, true);
        }

        return "delete success";
    }

    /**
     * 根据组成员查询组任务列表
     *
     * @return
     */
    @GetMapping("findTaskGroup")
    @ResponseBody
    public String findTaskGroup(String userId) {
        List<Task> zs = taskService.createTaskQuery().taskCandidateOrAssigned(userId).list();
        return "find success";
    }

    /**
     * 认领任务
     *
     * @param taskId
     * @param userId
     * @return
     */
    @GetMapping("claim")
    @ResponseBody
    public String claim(String taskId, String userId) {
        taskService.claim(taskId, userId);
        return "claim success";
    }

    /**
     * @param userId 查找指定人的任务
     * @return
     */
    @GetMapping("findTask")
    @ResponseBody
    public String findTask(String userId) {
        List<Task> list = taskService.createTaskQuery().taskAssignee(userId).list();
        return "find success";
    }

    /**
     * @return 获取下一步连线的名称  以此获取前端所需要显示的按钮
     * 此方法不兼容Activiti6.0
     */
    @GetMapping("getLineName")
    @ResponseBody
    public String getLineName(String taskId) {

        List<String> list = new ArrayList<>();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(task.getProcessDefinitionId());

        //获取当前的活动ID
        String processInstanceId = task.getProcessInstanceId();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        String activitiId = processInstance.getActivityId();
        ActivityImpl activity = processDefinitionEntity.findActivity(activitiId);
        List<PvmTransition> pvmList = activity.getOutgoingTransitions();
        if (pvmList != null && pvmList.size() > 0) {
            for (PvmTransition pvm : pvmList) {
                String name = (String) pvm.getProperty("name");
                if (StringUtils.isNotBlank(name)) {
                    list.add(name);
                } else {
                    list.add("确认");
                }
            }
        }
        return "getLineNameSuccess";
    }

    /**
     * 完成任务与添加流程批注
     *
     * @param taskId 任务ID
     * @param message 按钮的文字
     * @return
     */
    @GetMapping("finishTask")
    @ResponseBody

    public String updateFinishTask(String taskId, String message) throws UnsupportedEncodingException {

            String userId = "c";
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            String processInstanceId = task.getProcessInstanceId();
            Authentication.setAuthenticatedUserId(userId);
            taskService.addComment(taskId, processInstanceId, "不符合实际情况");
            Map<String, Object> map = new HashMap<>();
            message = new String(message.getBytes("ISO8859-1"), "UTF-8");
            map.put("message", message);
            taskService.complete(taskId, map);


        return "finish success";
    }


    /**
     * 查看流程批注(使用当前任务ID)
     *
     * @return
     */
    @ResponseBody
    @GetMapping("findComment")
    public String findComment(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        List<Comment> processInstanceComments = taskService.getProcessInstanceComments(processInstanceId);
        return "find success";
    }

}
