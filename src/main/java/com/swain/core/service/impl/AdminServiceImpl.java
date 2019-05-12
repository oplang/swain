package com.swain.core.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.swain.core.common.enums.UserPermissionEnum;
import com.swain.core.common.vo.MachineVO;
import com.swain.core.dal.domain.Machine;
import com.swain.core.dal.domain.Material;
import com.swain.core.dal.domain.User;
import com.swain.core.dal.manager.MachineManager;
import com.swain.core.dal.manager.MaterialManager;
import com.swain.core.dal.manager.UserManager;
import com.swain.core.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    /**用户管理*/
    @Autowired
    private UserManager userManager;

    @Override
    public List<User> getAllUsers() {
        //获取全部用户信息，按照时间逆序显示
        return userManager.selectList(new EntityWrapper<User>()
                .orderDesc(Collections.singleton("gmt_modified")));
    }

    @Override
    public User getUserById(Long id) {
        return userManager.selectById(id);
    }

    @Override
    public Integer deleteUserById(Long id) {
        return userManager.deleteById(id);
    }

    @Override
    public Integer updateUser(User user) {
        return userManager.updateById(user);
    }

    @Override
    public List<User> getAllStaff() {
        return userManager.selectList(
                new EntityWrapper<User>()
                        .eq("permission", UserPermissionEnum.STAFF.getCode()));
    }

    @Override
    public User checkLogin(User user) {
        User loginUser = userManager.selectOne(new EntityWrapper<User>()
                .eq("username",user.getUsername())
                .eq("password",user.getPassword()));
        if(loginUser == null){
            return new User();
        }
        return loginUser;
    }

    @Override
    public Integer addUser(User user) {
        return userManager.insert(user);
    }


    /**机器管理*/
    @Autowired
    private MachineManager machineManager;

    @Override
    public List<MachineVO> getAllMachines() {
        List<Machine> machineList = machineManager.selectList(new EntityWrapper<Machine>().orderDesc(Collections.singleton("gmt_modified")));
        List<MachineVO> machineVOList = new ArrayList<>();

        Map<Long,String> machineUserNameMap = new HashMap<Long,String>();

        for(Machine machine:machineList){
            if(machineUserNameMap.containsKey(machine.getMachineId()) == false){
                String machineUserName = userManager.selectById(machine.getMachineUserId()).getUsername();
                machineUserNameMap.put(machine.getMachineId(),machineUserName);
            }

            MachineVO machineVO = new MachineVO();
            machineVO.setMachineId(machine.getMachineId());
            machineVO.setMachineName(machine.getMachineName());
            machineVO.setMachineType(machine.getMachineType());
            machineVO.setMachineUserName(machineUserNameMap.get(machine.getMachineId()));
            machineVO.setGmtCreate(machine.getGmtCreate());
            machineVO.setGmtModified(machine.getGmtModified());
            machineVOList.add(machineVO);
        }
        /**一个优化查询的解决方案*/
        /*List<Machine> machineList = machineManager.selectList(new EntityWrapper<Machine>().orderDesc(Collections.singleton("gmt_modified")));
        List<MachineVO> machineVOList = new ArrayList<>();

        //存放machineUserIds, 去重
        List<Long> machineUserIds = Lists.transform(machineList, Machine::getMachineUserId);

        Map<Long, User> userMap = userManager.selectList(new EntityWrapper<User>()
                .in("user_id", machineUserIds)).stream()
                .collect(Collectors.toMap(User::getUserId, x -> x));*/
        return machineVOList;
    }

    @Override
    public MachineVO getMachineById(Long id) {
        Machine machine = machineManager.selectById(id);
        User user = userManager.selectById(machine.getMachineUserId());
        MachineVO machineVO = new MachineVO();
        machineVO.setMachineId(machine.getMachineId());
        machineVO.setMachineName(machine.getMachineName());
        machineVO.setMachineType(machine.getMachineType());
        machineVO.setMachineUserName(user.getUsername());
        machineVO.setGmtCreate(machine.getGmtCreate());
        machineVO.setGmtModified(machine.getGmtModified());
        return machineVO;
    }

    @Override
    public Integer addMachine(Machine machine) {
        return machineManager.insert(machine);
    }

    @Override
    public Integer deleteMachineById(Long id) {
        return machineManager.deleteById(id);
    }

    @Override
    public Integer updateMachine(Machine machine) {
        return machineManager.updateById(machine);
    }


    /**物料的管理
     */
    @Autowired
    private MaterialManager materialManager;
    @Override
    public Integer addMaterial(Material material) {
        //若物料已存在，则增加库存即可
        Material m = materialManager.selectOne(new EntityWrapper<Material>().eq("material_name",material.getMaterialName()));
        if(m.getMaterialName() != null){
            m.setMaterialTotal(m.getMaterialTotal().add(material.getMaterialTotal()));
            return materialManager.updateById(m);
        }
        return materialManager.insert(material);
    }

    @Override
    public Integer deleteMaterialById(Long id) {
        return materialManager.deleteById(id);
    }

    @Override
    public Integer updateMaterial(Material material) {
        return materialManager.updateById(material);
    }

    @Override
    public List<Material> getAllMaterials() {
        return materialManager.selectList(new EntityWrapper<Material>().orderDesc(Collections.singleton("gmt_modified"))
            );
    }

    @Override
    public Material getMaterialById(Long id) {
        return materialManager.selectById(id);
    }
}
