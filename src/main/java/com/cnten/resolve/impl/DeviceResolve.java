package com.cnten.resolve.impl;

import com.cnten.redis.impl.RedisHelperImpl;
import com.cnten.resolve.AbstractResolve;
import com.cnten.sender.DeviceSender;
import com.cnten.utils.CrcUtils;
import com.cnten.utils.StringUtils;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.util.StringUtil;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther: gaoTeng
 * @Date: 2018/9/4 0004
 * @Description:
 */
@Component
public class DeviceResolve implements AbstractResolve {

	private static final Logger log = LoggerFactory.getLogger(DeviceResolve.class);

	private StringBuffer dataBuffer = new StringBuffer();
	@Autowired
	private DeviceSender deviceSender;
	@Autowired
	private RedisHelperImpl redisHelperImpl;

	@Value("${backFlag.isBack}")
	private boolean isBack;
	@Value("${cache.data-key}")
	private String dataKey;

	@Override
	public void resolve(String data, NetSocket socket) {
		log.info("server receive data:"+data);
		String sData = replaceBlank(data);
		dataBuffer.append(sData);
		if (!dataBuffer.toString().startsWith("$")) {
			if (dataBuffer.toString().contains("$")){
				int index = dataBuffer.toString().indexOf("$");
				String str = dataBuffer.toString().substring(index,dataBuffer.toString().length());
				dataBuffer.setLength(0);
				dataBuffer.append(str);
			}
		}
		if (dataBuffer.toString().startsWith("$")) {
			int[] indexArr = getIndexofChar(dataBuffer.toString(), "$");
			if (indexArr.length == 2){
				String msg =  dataBuffer.toString().substring(indexArr[0],indexArr[1]+1);
				String nextData = dataBuffer.toString().substring(indexArr[1]+1,dataBuffer.toString().length());
				dataBuffer.setLength(0);
				dataBuffer.append(nextData);
				log.info("server receive msg:"+msg);
				log.info("server receive dataBuffer:"+dataBuffer.toString());
				if(isBack){
					String substring = msg.substring(1, 6);
					if("BDTXR".equals(substring)) {
						String bdCardMsg = dealBdCardData(msg);
						String value = String.valueOf(redisHelperImpl.getValue(dataKey));
						if(StringUtil.isEmpty(value)){
							value = "";
						}
						redisHelperImpl.valuePut(dataKey,value+bdCardMsg);
					}
				}
				deviceSender.send(msg);
			}
		}
		socket.close();
	}


	/**
	 * 根据规则替换北斗卡号，拼成新的报文
	 *
	 * $BDTXR开头的两种报文，长报文生成异或校验码，和校验码；短报文只生成异或检验码
	 *
	 * 原始报文格式  $BDTXR,1,0920694,2,,A450D60119040312150042CB6666401A3D7100000000000000000000000000A1F00000000000000000000000000000000000000000*48
	 * 新报文格式  $CCTXA,0920694,1,2,A409206940119040312150042CB6666401A3D7100000000000000000000000000A1F00000000000000000000000000000000000000000*48
	 * @param msg
	 * @return
	 */
	private String dealBdCardData(String msg) {
		log.info("length:"+msg.length());
		String newDataMsg = null;
		String messageData = null; //数据部分

		StringBuffer sBuffer = new StringBuffer();//新数据的sBuffer
		String[] msgArr = msg.split(",");
		StringBuilder sBuilder = new StringBuilder(msgArr[5]);
		sBuffer.append("$CCTXA,");//新数据报文头
		sBuffer.append(msgArr[2]);
		sBuffer.append(",1,2,");
		StringBuilder replaceBuilder = sBuilder.replace(2, 6, msgArr[2]);//替换北斗卡号
		sBuffer.append(replaceBuilder.toString());
		String cacheData = sBuffer.toString(); //未和校验，异或校验的新数据
		if (msg.length() > 89){  //$BDTXR开头长报文
			//首先生成和校验码
			String[] cacheDataArr = cacheData.split(",");
			messageData = cacheDataArr[4].substring(2,66); //生成和检验的数据部分
			byte b = CrcUtils.SumCheck(StringUtils.toBytes(messageData));
			String hexStr = StringUtils.bytesToHexString(b).toUpperCase();//和校验码
			StringBuilder stringBuilder = new StringBuilder(cacheDataArr[4]);
			StringBuilder replace = stringBuilder.replace(67, 69, hexStr);
			StringBuffer delete = sBuffer.delete(sBuffer.toString().lastIndexOf(",")+1, sBuffer.toString().length());
			StringBuffer appendBuffer = delete.append(replace); //未异或校验的新数据
			//然后生成异或校验码
			String xorData = appendBuffer.toString();
			String xorMessage1 = StringUtils.subString(xorData, "$", "*");
			String xor = CrcUtils.getXOR(xorMessage1.getBytes()).toUpperCase();
			StringBuffer replace1 = appendBuffer.replace(xorData.length() - 2, xorData.length(), xor);
			replace1.delete(replace1.length()-5,replace1.length()-3);//由于将原始报文的4位换成了北斗卡号7位，所以将长报文后面补的0删除三位
			newDataMsg = replace1.toString();
		} else {  //$BDTXR开头短报文
			//短报文只生成异或校验码
			String xorMessage1 = StringUtils.subString(cacheData, "$", "*");
			String xor = CrcUtils.getXOR(xorMessage1.getBytes()).toUpperCase();
			StringBuffer replace1 = sBuffer.replace(cacheData.length() - 2, cacheData.length(), xor);
			newDataMsg = replace1.toString();
		}
		log.info("newDataMsg:"+newDataMsg);
		return newDataMsg;
	}

	/**
	 * 获取某一个字符串中字符的位置
	 * @param str
	 * @param c
	 * @return
	 */
	private static int[] getIndexofChar(String str,String c){
		int index = 0;
		int[] ary = {};
		while((index = str.indexOf(c)) != -1){
			ary= Arrays.copyOf(ary, ary.length+1);
			ary[ary.length-1] = index;
			str = str.substring(index + c.length());
		}
		return ary;
	}

	/**
	 * 去除字符串中的空格、回车、换行符、制表符
	 * @param str
	 * @return
	 */
	public static String replaceBlank(String str) {
		String dest = "";
		if (str!=null) {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n|\r\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

}
