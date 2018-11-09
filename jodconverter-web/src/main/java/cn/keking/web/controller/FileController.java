package cn.keking.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import cn.keking.model.ReturnResponse;
import cn.keking.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author yudian-it
 * @date 2017/12/1
 */
@RestController
public class FileController {
    @Value("${file.dir}")
    String fileDir;
    @Value("${server.url}")
    String fileServer;
    @Autowired
    FileUtils fileUtils;
    String demoDir = "transform";
    String demoPath = demoDir + File.separator;

    @RequestMapping(value = "fileUpload", method = RequestMethod.POST)
    public String fileUpload(@RequestParam("file") MultipartFile file,
                             HttpServletRequest request) throws JsonProcessingException {
        String fileName = file.getOriginalFilename();
        File outFile = new File(fileDir + demoPath);
        if (!outFile.exists()) {
            outFile.mkdirs();
        }
        try(InputStream in = file.getInputStream();
            OutputStream ot = new FileOutputStream(fileDir + demoPath + fileName)){
            byte[] buffer = new byte[1024];
            int len;
            while ((-1 != (len = in.read(buffer)))) {
                ot.write(buffer, 0, len);
            }
            String url = fileServer+demoPath+fileName;
            return new ObjectMapper().writeValueAsString(new ReturnResponse<String>(0, "SUCCESS", url));
        } catch (IOException e) {
            e.printStackTrace();
            return new ObjectMapper().writeValueAsString(new ReturnResponse<String>(1, "FAILURE", null));
        }
    }

    @RequestMapping(value = "deleteFile", method = RequestMethod.GET)
    public String deleteFile(String fileName) throws JsonProcessingException {
        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        }
        File file = new File(fileDir + demoPath + fileName);
        if (file.exists()) {
            file.delete();
        }
        return new ObjectMapper().writeValueAsString(new ReturnResponse<String>(0, "SUCCESS", null));
    }

    @RequestMapping(value = "listFiles", method = RequestMethod.GET)
    public String getFiles() throws JsonProcessingException {
        List<Map<String, String>> list = Lists.newArrayList();
        File file = new File(fileDir + demoPath);
        if (file.exists()) {
            Arrays.stream(file.listFiles()).forEach(file1 -> list.add(ImmutableMap.of("fileName", demoDir + "/" + file1.getName())));
        }
        return new ObjectMapper().writeValueAsString(list);
    }

    private String getFileName(String name) {
        String suffix = name.substring(name.lastIndexOf("."));
        String nameNoSuffix = name.substring(0, name.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        return uuid + "-" + nameNoSuffix + suffix;
    }

    /**
     * 是否存在该类型的文件
     * @return
     * @param fileName
     */
    private boolean existsTypeFile(String fileName) {
        boolean result = false;
        String suffix = fileUtils.getSuffixFromFileName(fileName);
        File file = new File(fileDir + demoPath);
        if (file.exists()) {
            for(File file1 : file.listFiles()){
                String existsFileSuffix = fileUtils.getSuffixFromFileName(file1.getName());
                if (suffix.equals(existsFileSuffix)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
