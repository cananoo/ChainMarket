pragma solidity ^0.4.25;

contract ArbitrationEvidence {
    // 仲裁结果结构体
    struct ArbitrationResult {
        string caseNo;           // 案件编号
        string voteRatio;        // 投票比例 (例如 "3:2")
        bool result;             // 仲裁结果 (true: 支持申请人, false: 驳回申请)
        string description;      // 结果描述
        uint256 timestamp;       // 时间戳
        bool exists;             // 是否存在
    }
    
    // 仲裁结果映射 (案件ID => 仲裁结果)
    mapping(uint256 => ArbitrationResult) public arbitrationResults;
    
    // 所有案件ID列表
    uint256[] public caseIds;
    
    // 记录仲裁结果事件
    event ResultRecorded(
        uint256 indexed caseId,
        string caseNo,
        string voteRatio,
        bool result,
        uint256 timestamp
    );
    
    /**
     * 记录仲裁结果
     * @param caseId 案件ID
     * @param caseNo 案件编号
     * @param voteRatio 投票比例
     * @param result 仲裁结果
     * @param description 结果描述
     */
    function recordResult(
        uint256 caseId,
        string caseNo,
        string voteRatio,
        bool result,
        string description
    ) public {
        // 检查案件是否已存在
        if (!arbitrationResults[caseId].exists) {
            // 添加到案件ID列表
            caseIds.push(caseId);
        }
        
        // 记录仲裁结果
        arbitrationResults[caseId] = ArbitrationResult({
            caseNo: caseNo,
            voteRatio: voteRatio,
            result: result,
            description: description,
            timestamp: now,
            exists: true
        });
        
        // 触发事件
        emit ResultRecorded(caseId, caseNo, voteRatio, result, now);
    }
    
    /**
     * 获取仲裁结果
     * @param caseId 案件ID
     * @return 案件编号、投票比例、仲裁结果、结果描述、时间戳、是否存在
     */
    function getResult(uint256 caseId) public view returns (
        string,
        string,
        bool,
        string,
        uint256,
        bool
    ) {
        ArbitrationResult storage result = arbitrationResults[caseId];
        return (
            result.caseNo,
            result.voteRatio,
            result.result,
            result.description,
            result.timestamp,
            result.exists
        );
    }
    
    /**
     * 检查案件是否已记录
     * @param caseId 案件ID
     * @return 是否已记录
     */
    function resultExists(uint256 caseId) public view returns (bool) {
        return arbitrationResults[caseId].exists;
    }
    
    /**
     * 获取案件总数
     * @return 案件总数
     */
    function getCaseCount() public view returns (uint256) {
        return caseIds.length;
    }
    
    /**
     * 获取所有案件ID
     * @return 案件ID数组
     */
    function getAllCaseIds() public view returns (uint256[]) {
        return caseIds;
    }
} 