package ua.kiev.prog;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/")
public class MyController {

    private Map<Long, byte[]> photos = new HashMap<Long, byte[]>();
    private List<MultipartFile> photosList = new ArrayList<>();

    @RequestMapping("/")
    public String onIndex() {
        return "index";
    }

    @RequestMapping(value = "/add_photo", method = RequestMethod.POST)
    public String onAddPhoto(Model model, @RequestParam MultipartFile photo) {
        if (photo.isEmpty())
            throw new PhotoErrorException();

        try {
            long id = System.currentTimeMillis();
            photos.put(id, photo.getBytes());
            photosList.add(photo);
            //IMPORTANT - > here we set attribute "photo_id" for jsp pages use of this photo
            model.addAttribute("photo_id", id);
            return "result";
        } catch (IOException e) {
            throw new PhotoErrorException();
        }
    }

    @RequestMapping("/photo/{photo_id}")
    public ResponseEntity<byte[]> onPhoto(@PathVariable("photo_id") long id) {
        return photoById(id);
    }

    @RequestMapping(value = "/view", method = RequestMethod.POST)
    public ResponseEntity<byte[]> onView(@RequestParam("photo_id") long id) {
        return photoById(id);
    }

    @RequestMapping("/delete/{photo_id}")
    public String onDelete(@PathVariable("photo_id") long id) {
        if (photos.remove(id) == null)
            throw new PhotoNotFoundException();
        else
            return "index";
    }

    @RequestMapping(value = "/statistics")
    public String onStatistics(Model model) {
        Set<Long> listOfId = photos.keySet();
        model.addAttribute("photo_id", listOfId);
        return "statistics";
    }

    @RequestMapping(value = "/delete_checkbox_photo", method = RequestMethod.POST)
    public String deleteCheckbox(Model model, @RequestParam(value = "deletePhoto", required = false) long[] deleteId) {
        if (deleteId != null) {
            for (long del : deleteId) {
                photos.remove(del);
            }
        }

        Set<Long> listId = photos.keySet();
        model.addAttribute("photo_id", listId);

        // return on statistics after delete
        return "statistics";
    }

    @RequestMapping(value = "/tozip", method = RequestMethod.GET)
    public void toZip(Model model, HttpServletResponse response) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        OutputStream outputStream = response.getOutputStream();
        byte[] buf = new byte[1024];

        try {
            ZipOutputStream zipOutputFile = new ZipOutputStream(outputStream);
            for (MultipartFile photo : photosList) {
                ByteArrayInputStream sourceStream = new ByteArrayInputStream(photo.getBytes());
                zipOutputFile.putNextEntry(new ZipEntry(photo.getOriginalFilename()));
                int len;
                while ((len = sourceStream.read(buf)) != -1) {
                    zipOutputFile.write(buf, 0, len);
                }
                zipOutputFile.closeEntry();
                sourceStream.close();
            }
            zipOutputFile.flush();
            zipOutputFile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("Content-disposition", "attachment; filename=/some.zip");
    }

    private ResponseEntity<byte[]> photoById(long id) {
        byte[] bytes = photos.get(id);
        if (bytes == null)
            throw new PhotoNotFoundException();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
    }
}
