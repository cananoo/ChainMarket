package org.fisco.bcos.asset.contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.abi.datatypes.Bool;
import org.fisco.bcos.sdk.abi.datatypes.Event;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.eventsub.EventCallback;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class Goods extends Contract {
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b506109b0806100206000396000f300608060405260043610610083576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806328d27f091461008857806329507f73146100c95780632f72439414610116578063b259003314610195578063b643d94c14610214578063c1f31d0d14610259578063df636dae14610284575b600080fd5b34801561009457600080fd5b506100b3600480360381019080803590602001909291905050506102d1565b6040518082815260200191505060405180910390f35b3480156100d557600080fd5b5061011460048036038101908080359060200190929190803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506102f4565b005b34801561012257600080fd5b5061014160048036038101908080359060200190929190505050610562565b604051808481526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200182151515158152602001935050505060405180910390f35b3480156101a157600080fd5b506101c0600480360381019080803590602001909291905050506105b9565b604051808481526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200182151515158152602001935050505060405180910390f35b34801561022057600080fd5b5061023f6004803603810190808035906020019092919050505061067e565b604051808215151515815260200191505060405180910390f35b34801561026557600080fd5b5061026e6106aa565b6040518082815260200191505060405180910390f35b34801561029057600080fd5b506102cf60048036038101908080359060200190929190803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506106b7565b005b6001818154811015156102e057fe5b906000526020600020016000915090505481565b600080600084815260200190815260200160002060010160149054906101000a900460ff16151561038d576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260148152602001807f476f6f647320646f6573206e6f7420657869737400000000000000000000000081525060200191505060405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff1614151515610432576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260198152602001807f496e76616c6964206e6577206f776e657220616464726573730000000000000081525060200191505060405180910390fd5b60008084815260200190815260200160002060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690508160008085815260200190815260200160002060010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055507f16b85f49bf01212961345d3016c9a531894accf62eb7680f2045d79185cc0ec0838284604051808481526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001935050505060405180910390a1505050565b60006020528060005260406000206000915090508060000154908060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16908060010160149054906101000a900460ff16905083565b60008060006105c661094a565b60008086815260200190815260200160002060606040519081016040529081600082015481526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160149054906101000a900460ff1615151515815250509050806000015181602001518260400151935093509350509193909250565b600080600083815260200190815260200160002060010160149054906101000a900460ff169050919050565b6000600180549050905090565b60008083815260200190815260200160002060010160149054906101000a900460ff1615151561074f576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260148152602001807f476f6f647320616c72656164792065786973747300000000000000000000000081525060200191505060405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff16141515156107f4576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260158152602001807f496e76616c6964206f776e65722061646472657373000000000000000000000081525060200191505060405180910390fd5b6060604051908101604052808381526020018273ffffffffffffffffffffffffffffffffffffffff168152602001600115158152506000808481526020019081526020016000206000820151816000015560208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160010160146101000a81548160ff02191690831515021790555090505060018290806001815401808255809150509060018203906000526020600020016000909192909190915055507fe7ebbf32a59d343a88752adff2a328119bc18e835ca52584eaca7344647afd4e8282604051808381526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019250505060405180910390a15050565b60606040519081016040528060008152602001600073ffffffffffffffffffffffffffffffffffffffff16815260200160001515815250905600a165627a7a723058204a9383e530a8882765048db46964ff1ef25cdac56d0c3ad562c3ec500e7399c50029"};

    public static final String BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"608060405234801561001057600080fd5b506109b0806100206000396000f300608060405260043610610083576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806330ddbfed1461008857806359055c491461010757806375e55cf11461014c5780638604fefc146101995780639476946a146101da578063afd500bf14610205578063bd20b27214610284575b600080fd5b34801561009457600080fd5b506100b3600480360381019080803590602001909291905050506102d1565b604051808481526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200182151515158152602001935050505060405180910390f35b34801561011357600080fd5b5061013260048036038101908080359060200190929190505050610396565b604051808215151515815260200191505060405180910390f35b34801561015857600080fd5b5061019760048036038101908080359060200190929190803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506103c2565b005b3480156101a557600080fd5b506101c460048036038101908080359060200190929190505050610630565b6040518082815260200191505060405180910390f35b3480156101e657600080fd5b506101ef610653565b6040518082815260200191505060405180910390f35b34801561021157600080fd5b5061023060048036038101908080359060200190929190505050610660565b604051808481526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200182151515158152602001935050505060405180910390f35b34801561029057600080fd5b506102cf60048036038101908080359060200190929190803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506106b7565b005b60008060006102de61094a565b60008086815260200190815260200160002060606040519081016040529081600082015481526020016001820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820160149054906101000a900460ff1615151515815250509050806000015181602001518260400151935093509350509193909250565b600080600083815260200190815260200160002060010160149054906101000a900460ff169050919050565b600080600084815260200190815260200160002060010160149054906101000a900460ff16151561045b576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004018080602001828103825260148152602001807f476f6f647320646f6573206e6f7420657869737400000000000000000000000081525060200191505060405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff1614151515610500576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004018080602001828103825260198152602001807f496e76616c6964206e6577206f776e657220616464726573730000000000000081525060200191505060405180910390fd5b60008084815260200190815260200160002060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690508160008085815260200190815260200160002060010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055507f3aaeff28cc51a91d6da893b7d0a8279446687b60736078d251518b0a1f06a1db838284604051808481526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001935050505060405180910390a1505050565b60018181548110151561063f57fe5b906000526020600020016000915090505481565b6000600180549050905090565b60006020528060005260406000206000915090508060000154908060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16908060010160149054906101000a900460ff16905083565b60008083815260200190815260200160002060010160149054906101000a900460ff1615151561074f576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004018080602001828103825260148152602001807f476f6f647320616c72656164792065786973747300000000000000000000000081525060200191505060405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff16141515156107f4576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004018080602001828103825260158152602001807f496e76616c6964206f776e65722061646472657373000000000000000000000081525060200191505060405180910390fd5b6060604051908101604052808381526020018273ffffffffffffffffffffffffffffffffffffffff168152602001600115158152506000808481526020019081526020016000206000820151816000015560208201518160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060408201518160010160146101000a81548160ff02191690831515021790555090505060018290806001815401808255809150509060018203906000526020600020016000909192909190915055507f0bd6539219e33f3f09af2ef30c2d80a3416b0a04f7f78976f25ba30633c00e778282604051808381526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019250505060405180910390a15050565b60606040519081016040528060008152602001600073ffffffffffffffffffffffffffffffffffffffff16815260200160001515815250905600a165627a7a72305820d2d694ba1dd7973ea05064d58b8c8a2a8407a0960cda1f291402c93b8229c87a0029"};

    public static final String SM_BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"goodsList\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_goodsId\",\"type\":\"uint256\"},{\"name\":\"_newOwner\",\"type\":\"address\"}],\"name\":\"transferOwnership\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"name\":\"goods\",\"outputs\":[{\"name\":\"goodsId\",\"type\":\"uint256\"},{\"name\":\"owner\",\"type\":\"address\"},{\"name\":\"exists\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_goodsId\",\"type\":\"uint256\"}],\"name\":\"getGoods\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"_goodsId\",\"type\":\"uint256\"}],\"name\":\"goodsExists\",\"outputs\":[{\"name\":\"\",\"type\":\"bool\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"getGoodsCount\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_goodsId\",\"type\":\"uint256\"},{\"name\":\"_owner\",\"type\":\"address\"}],\"name\":\"createGoods\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"goodsId\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"owner\",\"type\":\"address\"}],\"name\":\"GoodsCreated\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"goodsId\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"from\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"to\",\"type\":\"address\"}],\"name\":\"OwnershipTransferred\",\"type\":\"event\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_GOODSLIST = "goodsList";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_GOODS = "goods";

    public static final String FUNC_GETGOODS = "getGoods";

    public static final String FUNC_GOODSEXISTS = "goodsExists";

    public static final String FUNC_GETGOODSCOUNT = "getGoodsCount";

    public static final String FUNC_CREATEGOODS = "createGoods";

    public static final Event GOODSCREATED_EVENT = new Event("GoodsCreated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}));
    ;

    protected Goods(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public BigInteger goodsList(BigInteger param0) throws ContractException {
        final Function function = new Function(FUNC_GOODSLIST, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public TransactionReceipt transferOwnership(BigInteger _goodsId, String _newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(_goodsId), 
                new org.fisco.bcos.sdk.abi.datatypes.Address(_newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] transferOwnership(BigInteger _goodsId, String _newOwner, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(_goodsId), 
                new org.fisco.bcos.sdk.abi.datatypes.Address(_newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForTransferOwnership(BigInteger _goodsId, String _newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(_goodsId), 
                new org.fisco.bcos.sdk.abi.datatypes.Address(_newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple2<BigInteger, String> getTransferOwnershipInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<BigInteger, String>(

                (BigInteger) results.get(0).getValue(), 
                (String) results.get(1).getValue()
                );
    }

    public Tuple3<BigInteger, String, Boolean> goods(BigInteger param0) throws ContractException {
        final Function function = new Function(FUNC_GOODS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Bool>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple3<BigInteger, String, Boolean>(
                (BigInteger) results.get(0).getValue(), 
                (String) results.get(1).getValue(), 
                (Boolean) results.get(2).getValue());
    }

    public Tuple3<BigInteger, String, Boolean> getGoods(BigInteger _goodsId) throws ContractException {
        final Function function = new Function(FUNC_GETGOODS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(_goodsId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Bool>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple3<BigInteger, String, Boolean>(
                (BigInteger) results.get(0).getValue(), 
                (String) results.get(1).getValue(), 
                (Boolean) results.get(2).getValue());
    }

    public Boolean goodsExists(BigInteger _goodsId) throws ContractException {
        final Function function = new Function(FUNC_GOODSEXISTS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(_goodsId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeCallWithSingleValueReturn(function, Boolean.class);
    }

    public BigInteger getGoodsCount() throws ContractException {
        final Function function = new Function(FUNC_GETGOODSCOUNT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public TransactionReceipt createGoods(BigInteger _goodsId, String _owner) {
        final Function function = new Function(
                FUNC_CREATEGOODS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(_goodsId), 
                new org.fisco.bcos.sdk.abi.datatypes.Address(_owner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] createGoods(BigInteger _goodsId, String _owner, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_CREATEGOODS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(_goodsId), 
                new org.fisco.bcos.sdk.abi.datatypes.Address(_owner)), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForCreateGoods(BigInteger _goodsId, String _owner) {
        final Function function = new Function(
                FUNC_CREATEGOODS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(_goodsId), 
                new org.fisco.bcos.sdk.abi.datatypes.Address(_owner)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple2<BigInteger, String> getCreateGoodsInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_CREATEGOODS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<BigInteger, String>(

                (BigInteger) results.get(0).getValue(), 
                (String) results.get(1).getValue()
                );
    }

    public List<GoodsCreatedEventResponse> getGoodsCreatedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(GOODSCREATED_EVENT, transactionReceipt);
        ArrayList<GoodsCreatedEventResponse> responses = new ArrayList<GoodsCreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            GoodsCreatedEventResponse typedResponse = new GoodsCreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.goodsId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.owner = (String) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeGoodsCreatedEvent(String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(GOODSCREATED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,fromBlock,toBlock,otherTopics,callback);
    }

    public void subscribeGoodsCreatedEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(GOODSCREATED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,callback);
    }

    public List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.goodsId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.from = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.to = (String) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeOwnershipTransferredEvent(String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,fromBlock,toBlock,otherTopics,callback);
    }

    public void subscribeOwnershipTransferredEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,callback);
    }

    public static Goods load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new Goods(contractAddress, client, credential);
    }

    public static Goods deploy(Client client, CryptoKeyPair credential) throws ContractException {
        return deploy(Goods.class, client, credential, getBinary(client.getCryptoSuite()), "");
    }

    public static class GoodsCreatedEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger goodsId;

        public String owner;
    }

    public static class OwnershipTransferredEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger goodsId;

        public String from;

        public String to;
    }
}
