package com.jw.controller;

import com.jw.Response;
import com.jw.mapper.AssignmentMapper;
import io.swagger.annotations.*;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import com.jw.service.IAssignmentService;
import com.jw.entity.Assignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baomidou.mybatisplus.core.metadata.IPage;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author x
 * @since 2020-10-13
 */

@lombok.Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="老师作业", description="")
class Data1 implements Serializable{
    List<Assignment> records;
    int total;
}
@Api(tags = {""})
@RestController
@RequestMapping("/assignment")
public class AssignmentController {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    private IAssignmentService assignmentService;

    @ApiOperation(value = "新增")
    @PostMapping()
    @ResponseBody
    public Response add(@RequestParam("file") MultipartFile file,
                        @RequestParam("mId") Integer mId,
                        @RequestParam("studentId") Integer studentId,
                        @RequestParam("score") Float score){
        String path = upload_assignment(file);
        Assignment assignment = new Assignment();
        assignment.setMId(mId);
        assignment.setStudentId(studentId);
        assignment.setScore(score);
        if (path == null)
            return Response.no("文件上传错误");
        assignment.setFile(path);
        int add = assignmentService.add(assignment);
        if (add != 0){
            return Response.yes(add);
        }
        return Response.no();
    }

    @ApiOperation(value = "删除")
    @DeleteMapping("{id}")
    public Response delete(@PathVariable("id") Long id){
        int delete = assignmentService.delete(id);
        if (delete!=0)
            return Response.yes(delete);
        return Response.no();
    }

    @ApiOperation(value = "更新")
    @PutMapping()
    public Response update(@RequestParam(value = "file", required = false) MultipartFile file,
                        @RequestParam("id") Integer id,
                        @RequestParam("mId") Integer mId,
                        @RequestParam("studentId") Integer studentId,
                        @RequestParam("score") Float score, @RequestParam("origin") String originFile){

        String path = file == null ? null : upload_assignment(file);
        String upath = path == null ? originFile : path;

        Assignment assignment = new Assignment();
        assignment.setId(id);
        assignment.setMId(mId);
        assignment.setStudentId(studentId);
        assignment.setScore(score);
        assignment.setFile(upath);

        int update = assignmentService.updateData(assignment);
        if (update != 0){
            return Response.yes(update);
        }
        return Response.no();
    }

    @Resource
    AssignmentMapper assignmentMapper;

    @ApiOperation(value = "查询分页数据")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", value = "页码"),
        @ApiImplicitParam(name = "pageCount", value = "每页条数"),
        @ApiImplicitParam(name = "type", value = "类型"),
        @ApiImplicitParam(name = "id", value = "ID")
    })
    @GetMapping()
    public Response findListByPage(@RequestParam Integer page,
                                   @RequestParam Integer pageCount,
                                   @RequestParam String type,
                                   @RequestParam Integer id){
        if (type.equals("student")){
            IPage<Assignment> listByPage = assignmentService.findListByPage(page, pageCount, id);
            if (listByPage!=null)
                return Response.yes(listByPage);
        }else if (type.equals("teacher")){
            int p = (page-1)*pageCount;
            List<Assignment> assignmentByTeacherId = assignmentMapper.findAssignmentByTeacherId(p, pageCount, id);

            Data1 data = new Data1();
            data.records = assignmentByTeacherId;
            data.total = assignmentMapper.findAssignmentByTeacherIdCount(id);
            if (assignmentByTeacherId!=null)
                return Response.yes(data);
        }else{
            IPage<Assignment> listByPage = assignmentService.findListByPage(page, pageCount);
            return Response.yes(listByPage);
        }
        return Response.no();
    }

    @ApiOperation(value = "id查询")
    @GetMapping("{id}")
    public Response findById(@PathVariable Long id){
        Assignment byId = assignmentService.findById(id);
        if (byId!=null){
            return Response.yes(byId);
        }
        return Response.no();
    }

    public String upload_assignment(MultipartFile file) {
        String fileName = file.getOriginalFilename();//获取文件名
        fileName = getFileName(fileName);
        String filepath = getUploadPath();
        if (!file.isEmpty()) {
            try{
                String s = filepath + File.separator + fileName;
                System.out.println(s);
                BufferedOutputStream out = new BufferedOutputStream(
                        new FileOutputStream(new File(s)));
                out.write(file.getBytes());
                out.flush();

                return s;
            } catch (IOException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 文件名后缀前添加一个时间戳
     */
    private String getFileName(String fileName) {
        int index = fileName.lastIndexOf(".");
        final SimpleDateFormat sDateFormate = new SimpleDateFormat("yyyymmddHHmmss");  //设置时间格式
        String nowTimeStr = sDateFormate.format(new Date()); // 当前时间
        fileName = fileName.substring(0, index) + "_" + nowTimeStr + fileName.substring(index);
        return fileName;
    }

    /**
     * 获取当前系统路
     */
    private String getUploadPath() {
        File path = null;
        try {
            path = new File(ResourceUtils.getURL("classpath:").getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (!path.exists()) path = new File("");
        File upload = new File(path.getAbsolutePath(), "static/upload/");
        if (!upload.exists()) upload.mkdirs();
        return upload.getAbsolutePath();
    }
}
