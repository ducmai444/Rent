package com.example.demo.controller;

import com.example.demo.message.GlobalMessage;
import com.example.demo.model.DTO.apartment.ApartmentDTO;
import com.example.demo.model.DTO.apartment.ApartmentUpdateDTO;
import com.example.demo.response.ApiResponse;
import com.example.demo.service.interfaces.ApartmentService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@AllArgsConstructor
@Log4j2
@RequestMapping("/apartments")
public class ApartmentController {
    private final ApartmentService apartmentService;
    private final String DEFAULT_PAGE_NUMBER = "0";
    private final String DEFAULT_PAGE_SIZE = "10";
    private final String DEFAULT_SORT_BY = "retailPrice";


    // [GET] /apartments/
    @GetMapping("")
    public ResponseEntity<Object> getApartmentList(
            @RequestParam(defaultValue =
                    DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue =
                    DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(defaultValue =
                    DEFAULT_SORT_BY) String sortBy) {

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sortBy));

        return ApiResponse.responseBuilder(HttpStatus.OK, GlobalMessage.SUCCESS,
                apartmentService.getAll(pageable));
    }

    // [GET] /apartments/:id
    @GetMapping("/{id}")
    public ResponseEntity<Object> getApartment(@PathVariable("id") String id) {
        return ApiResponse.responseBuilder(HttpStatus.OK, GlobalMessage.SUCCESS, apartmentService.getApartmentDTO(id));
    }

    // [POST] /apartments/add
    @PostMapping("/add")
    public ResponseEntity<Object> save(@RequestBody ApartmentDTO apartmentDTO) {
        return ApiResponse.responseBuilder(HttpStatus.CREATED, GlobalMessage.SUCCESS, apartmentService.create(apartmentDTO));
    }

    // [POST] /apartments/update/:id
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable("id") String id, @RequestBody ApartmentUpdateDTO apartmentUpdateDTO) {
        return ApiResponse.responseBuilder(HttpStatus.OK, GlobalMessage.SUCCESS, apartmentService.update(id, apartmentUpdateDTO));
    }

    // [DELETE] /apartments/delete/:id
    @PreAuthorize("hasAuthority('MANAGER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable String id) {
        return ApiResponse.responseBuilder(HttpStatus.OK, GlobalMessage.SUCCESS, apartmentService.delete(id));
    }

    // [POST] /apartments/import
    @PostMapping("/import")
    public ResponseEntity<Object> importCsvApartments(@RequestParam("file") MultipartFile[] files) {
        return ApiResponse.responseBuilder(HttpStatus.OK, GlobalMessage.SUCCESS,
                apartmentService.loadApartments(files));
    }

    // [GET] /apartments/search
    @GetMapping("/search")
    public ResponseEntity<Object> searchApartments(@RequestParam(defaultValue =
            DEFAULT_PAGE_NUMBER) int page,
                                                   @RequestParam(defaultValue =
                                                           DEFAULT_PAGE_SIZE) int pageSize,
                                                   @RequestParam(defaultValue =
                                                           DEFAULT_SORT_BY) String sortBy,
                                                   @RequestParam(value = "q") String query) {

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sortBy));

        return ApiResponse.responseBuilder(HttpStatus.OK, GlobalMessage.SUCCESS,
                apartmentService.search(query, pageable));
    }

    // [GET] /apartments/export
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportCsv(@RequestParam(value = "getTemplate", defaultValue = "false") boolean getTemplate) {
        HttpHeaders responseHeader = new HttpHeaders();

        File file = null;
        try {

            file = apartmentService.exportCsv(getTemplate);
            byte[] data = FileUtils.readFileToByteArray(file);

            responseHeader.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            responseHeader.setContentDisposition(ContentDisposition.attachment().filename(file.getName()).build());

            InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(data));
            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

            return new ResponseEntity<>(inputStreamResource, responseHeader, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (file != null)
                file.delete();
        }
    }

    // [GET] /apartments/statistic
    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/statistic")
    public ResponseEntity<Object> statistic() {
        return ApiResponse.responseBuilder(HttpStatus.OK, GlobalMessage.SUCCESS,
                apartmentService.statistic());
    }
}
