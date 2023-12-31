package com.favorsoft.mplatform.batch;

import com.favorsoft.mplatform.cdn.domain.DataProcessInstance;
import com.favorsoft.mplatform.cdn.domain.DataProcessInstanceFlow;
import com.favorsoft.mplatform.cdn.dto.CodeGroupDTO;
import com.favorsoft.mplatform.cdn.dto.MasterDTO;
import com.favorsoft.mplatform.cdn.dto.PropValue;
import com.favorsoft.mplatform.cdn.enums.ApprovalFlowStatus;
import com.favorsoft.mplatform.cdn.enums.ApprovalStatus;
import com.favorsoft.mplatform.cdn.enums.FlowStep;
import com.favorsoft.mplatform.cdn.service.CodeGroupService;
import com.favorsoft.mplatform.cdn.service.MasterService;
import com.favorsoft.mplatform.cdn.service.dataprocess.DataProcessInstanceService;
import com.favorsoft.mplatform.cdn.service.dataprocess.DataProcessManager;
import com.favorsoft.mplatform.support.ApplicationContextProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class DataProcessBatchService {

    private final DataProcessInstanceService dataProcessInstanceService;

    private final CodeGroupService codeGroupService;

    private final MasterService masterService;

    private static Logger logger = LoggerFactory.getLogger(DataProcessBatchService.class);

    public DataProcessBatchService(DataProcessInstanceService dataProcessInstanceService, CodeGroupService codeGroupService, MasterService masterService){
        this.dataProcessInstanceService = dataProcessInstanceService;
        this.codeGroupService = codeGroupService;
        this.masterService = masterService;
    }

    //@Scheduled(cron = "0 * * * * ?")
    public void createDataProcess() throws Exception {
        List<DataProcessInstance> dataProcessInstanceList = dataProcessInstanceService.getInstanceList(ApprovalStatus.CREATE);

        for(DataProcessInstance instance: dataProcessInstanceList){
            instance.setStatus(ApprovalStatus.PROCESS);
            instance.setStartDate(new Date());

            List<DataProcessInstanceFlow> instanceFlowList = instance.getApprovalInstanceFlowList();
            DataProcessInstanceFlow currentInstanceFlow = instanceFlowList.stream().filter(s-> s.getFlowStep().equals(FlowStep.DRAFT)).findAny().get();
            currentInstanceFlow.setArriveDate(new Date());
            currentInstanceFlow.setProcessDate(new Date());
            currentInstanceFlow.setApprovalFlowStatus(ApprovalFlowStatus.ACCEPT);

            instance.setCurrentStep(currentInstanceFlow.getNextFlowStep());

            DataProcessInstanceFlow nextInstanceFlow = instanceFlowList.stream().filter(s-> s.getFlowStep().equals(currentInstanceFlow.getNextFlowStep())).findAny().get();
            nextInstanceFlow.setArriveDate(new Date());
            nextInstanceFlow.setApprovalFlowStatus(ApprovalFlowStatus.WAIT);

            instance.setCurrentUser(nextInstanceFlow.getUser());

            DataProcessManager dataProcessManager = newInstance(instance.getManageClass());

            dataProcessManager.changeStep(currentInstanceFlow, nextInstanceFlow);
            dataProcessManager.startDataProcess(dataProcessInstanceService.save(instance));
        }
    }

    //@Scheduled(cron = "20 * * * * ?")
    public void activeDataProcess() throws Exception {
        List<DataProcessInstance> approvalInstanceList = dataProcessInstanceService.getInstanceList(ApprovalStatus.PROCESS);

        for(DataProcessInstance instance: approvalInstanceList){
           DataProcessManager dataProcessManager = newInstance(instance.getManageClass());
           if(instance.getValidDate().getTime() < new Date().getTime()){
               instance.setStatus(ApprovalStatus.REJECT);
               instance.setEndDate(new Date());

               List<DataProcessInstanceFlow> instanceFlowList = instance.getApprovalInstanceFlowList();

               DataProcessInstanceFlow currentInstanceFlow = instanceFlowList.stream().filter(s-> s.getApprovalFlowStatus().equals(ApprovalFlowStatus.WAIT)).findFirst().get();

               currentInstanceFlow.setApprovalFlowStatus(ApprovalFlowStatus.REJECT);
               currentInstanceFlow.setApprovalMessage("[TIMEOUT] Reject from system.");
               currentInstanceFlow.setProcessDate(new Date());
               currentInstanceFlow.setNextFlowStep(null);

               dataProcessManager.rejectDataProcess(instance);
           }else{
               List<DataProcessInstanceFlow> instanceFlowList = instance.getApprovalInstanceFlowList();
               DataProcessInstanceFlow currentInstanceFlow = instanceFlowList.stream().filter(s-> s.getFlowStep().equals(instance.getCurrentStep())).findAny().get();
               DataProcessInstanceFlow nextInstanceFlow = instanceFlowList.stream().filter(s-> s.getFlowStep().equals(currentInstanceFlow.getNextFlowStep())).findAny().orElse(new DataProcessInstanceFlow());

               if(currentInstanceFlow.getApprovalFlowStatus().equals(ApprovalFlowStatus.ACCEPT)){
                   if(currentInstanceFlow.getNextFlowStep().equals(FlowStep.FINAL)){
                       instance.setCurrentStep(FlowStep.FINAL);
                       instance.setStatus(ApprovalStatus.DATA_SAVING);
                   }else{
                       instance.setCurrentStep(currentInstanceFlow.getNextFlowStep());
                       instance.setCurrentUser(nextInstanceFlow.getUser());

                       nextInstanceFlow.setArriveDate(new Date());
                       nextInstanceFlow.setApprovalFlowStatus(ApprovalFlowStatus.WAIT);
                   }

                   dataProcessManager.acceptDataProcess(instance);
                   dataProcessManager.changeStep(currentInstanceFlow, nextInstanceFlow);
               }else if(currentInstanceFlow.getApprovalFlowStatus().equals(ApprovalFlowStatus.REJECT)){
                   instance.setStatus(ApprovalStatus.REJECT);
                   instance.setEndDate(new Date());

                   dataProcessManager.rejectDataProcess(instance);
               }
           }

            dataProcessInstanceService.save(instance);
        }
    }

    //@Scheduled(cron = "40 * * * * ?")
    public void finishDataProcess() throws Exception {
        Gson gson = new Gson();

        List<DataProcessInstance> approvalInstanceList = dataProcessInstanceService.getInstanceList(ApprovalStatus.DATA_SAVING);

        for(DataProcessInstance instance: approvalInstanceList){
            DataProcessManager dataProcessManager = newInstance(instance.getManageClass());

            DataProcessInstanceFlow lastFlow = instance.getApprovalInstanceFlowList().stream().filter(s-> s.getNextFlowStep().equals(FlowStep.FINAL)).findAny().get();
            try{
                if("MASTER".equals(instance.getDataType())){
                    MasterDTO masterDTO = new MasterDTO();
                    masterDTO.setMasterId(instance.getDataId());
                    masterDTO.setDomainId(instance.getDataGroup());
                    Type listType = new TypeToken<ArrayList<PropValue>>(){}.getType();
                    masterDTO.setData(gson.fromJson(lastFlow.getData(), listType));

                    this.masterService.saveMaster(masterDTO);
                }else if("CODE_GROUP".equals(instance.getDataType())){
                    CodeGroupDTO codeGroupDTO = new CodeGroupDTO();
                    codeGroupDTO.setCode(instance.getDataId());
                    codeGroupDTO.setCodeGroupId(instance.getDataGroup());
                    Type listType = new TypeToken<ArrayList<PropValue>>(){}.getType();
                    codeGroupDTO.setData(gson.fromJson(lastFlow.getData(), listType));
                    codeGroupDTO.setReference(instance.getInstanceId());

                    this.codeGroupService.saveCode(codeGroupDTO);
                }else{
                    throw new Exception("Can not match data type");
                }
                instance.setStatus(ApprovalStatus.FINISH);
                instance.setEndDate(new Date());
                instance.setCurrentUser(null);
                instance.setCurrentStep(null);

                dataProcessManager.endDataProcess(dataProcessInstanceService.save(instance));

            }catch(Exception e){
                instance.setStatus(ApprovalStatus.DATA_ERROR);
                instance.setEndDate(new Date());
                instance.setErrorMessage(e.getMessage());
                dataProcessInstanceService.save(instance);
            }
        }
    }

    private DataProcessManager newInstance(String manageClass) throws Exception {
        return (DataProcessManager) ApplicationContextProvider.getApplicationContext().getBean(Class.forName(manageClass));
    }
}
