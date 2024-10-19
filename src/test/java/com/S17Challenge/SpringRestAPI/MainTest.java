package com.S17Challenge.SpringRestAPI;

import com.S17Challenge.SpringRestAPI.entity.Course;
import com.S17Challenge.SpringRestAPI.entity.Grade;
import com.S17Challenge.SpringRestAPI.exceptions.ApiErrorResponse;
import com.S17Challenge.SpringRestAPI.exceptions.ApiExceptions;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // testlerin bir spring konteynır içinde çalışmasını sağlar. Böylece uygulamanın tüm bileşenlerini(servis vs.) içeririr.
@AutoConfigureMockMvc // HTTP isteklerini simüle etmek ve bu isteklerin yanıtlarını test etmek için kullanılır.Böylece gerçek bir sunucu başlatmadan API'nizi test edebilirsiniz.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Test metotlarının çalıştırılma sırasını belirlemek için kullanılır.
class MainTest {

    @Autowired
    private Environment env; // application.properties veya application.yml dosyasındaki ayaralara erişimi sağlar.

    @Autowired
    private MockMvc mockMvc; // API testleri için HTTP isteklerini simüle etmek için kullanıyoruz.

    @Autowired
    private ObjectMapper objectMapper; // JSON verilerini Java nesnelerine dönüştürmek yada tam tersini yapmak için kullanılan Jackson kütüphanesinin bir bileşeni.

    private Course course;

    @BeforeEach
    void setUp() throws Exception {
        course = new Course(1, "Introduction to Spring", 3, new Grade(1, "A"));
        createCourse(course); // Helper method - bu kursun API üzerinden veritabanına eklenmesini sağlıyor.
    }

    private void createCourse(Course course) throws Exception {
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON) // Gönderilen isteğinin içeriğinin JSON olduğunu belirtir.
                        .content(objectMapper.writeValueAsString(course))) // course nesnesini JSON verisine dönüştürür, POST isteği ile API'ye gönderilir.
                .andExpect(status().isCreated()); // isteğin başarılı bir şekilde işlendiğini ve HTTP durum kodunun 201 olduğunu kontrol eder.
    }

    @Test // JUnit tarafından test metodu olarak işaretlenen bir metottur.
    void testServerProperties() {
        assertThat(env.getProperty("server.port")).isEqualTo("9000"); // portun 9000 olduğunu doğrular
        assertNotNull(env.getProperty("server.servlet.context-path")); // ayarının null olmadığını doğrular
        assertThat(env.getProperty("server.servlet.context-path")).isEqualTo("/workintech"); // uygulamanın bağlam yolunun "/workintech" olduğunu doğrular
    }

    @Test
    void testCreateCourse() throws Exception {
        Course newCourse = new Course(2, "Yeni Kurs", 3, new Grade(1, "B"));
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourse)))
                .andExpect(status().isCreated()) // 201 bekleyin
                .andExpect(jsonPath("$.course.name", is(newCourse.getName())));
    }

    @Test
    void testHandleApiException() throws Exception {
        mockMvc.perform(get("/courses/{name}", "testCourseName")) // Başına / eklendi
                .andExpect(status().isNotFound()) // İstenilen kaynak bulunamadığı zaman 404 durum kodunu verecek.
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // API yanıtının içeriğinin JSON olduğunu söyler.
                .andExpect(jsonPath("$.message").isNotEmpty()) // JSON yapısındaki message alanının boş olmadığını kontrol eder.
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value())); // JSON yapısındaki status alanının 404 olduğunu kontrol eder.
    }


    @Test
    void testCreateCourseValidationFailure() throws Exception {
        Course invalidCourse = new Course(null,null,null,null); // geçersiz bir course nesnesi oluşturuld.
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCourse)))  // invalidCourse nesnesini JSON formatına dönüştürmek için ObjectMapper kullanılır.
                .andExpect(status().isBadRequest()) // API yanıtının durum kodunun 400 olduğunu kontrol eder.
                .andExpect(jsonPath("$.message").isNotEmpty()) //  JSON yapısındaki message alanının boş olmadığını kontrol eder.
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value())); // JSON yapısındaki status alanının 400 olduğunu kontrol eder.
    }

    @Test
    void testGetAllCourses() throws Exception {
        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk()) //  API yanıtının durum kodunun 200 (OK) olduğunu kontrol eder.
                .andExpect(jsonPath("$",hasSize(greaterThanOrEqualTo(0)))); // SON yapısındaki kök (root) dizininin boyutunun 0 veya daha fazla olduğunu kontrol eder.
        // Yani, en az bir kursun mevcut olup olmadığını doğrular.
    }

    @Test
    void testGetCourseByName() throws Exception {
        mockMvc.perform(get("/courses/{name}", course.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name",is(course.getName()))); // JSON yapısındaki name alanının, istenen kursun adını (course.getName()) doğru bir şekilde döndürdüğünü kontrol eder.
    }

    @Test
    void testUpdateCourse() throws Exception {
        course.setName("Advanced Spring");
        mockMvc.perform(put("/courses/{id}",course.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(course)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteCourse() throws Exception {
        mockMvc.perform(delete("/courses/{id}",course.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void testApiErrorResponseFields() {
        ApiErrorResponse errorResponse = new ApiErrorResponse(404,"Not Found",System.currentTimeMillis());
        assertEquals(404, errorResponse.getStatus()); // yanıtın 404 olup olmadığını kontrol eder.
        assertEquals("Not Found", errorResponse.getMessage()); // Hata mesajının "not found" olup olmadığını kontrol eder.
    }

    @Test
    void testApiExceptionCreation() {
        ApiExceptions exception = new ApiExceptions("Test exception message",HttpStatus.NOT_FOUND);
        assertEquals("Test exception message",exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertTrue(exception instanceof RuntimeException); // exception nesnesinin RuntimeException sınıfının bir örneği olup olmadığını kontrol eder.
    }
}

