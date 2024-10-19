package com.S17Challenge.SpringRestAPI.controller;

import com.S17Challenge.SpringRestAPI.entity.ApiResponse;
import com.S17Challenge.SpringRestAPI.entity.Course;
import com.S17Challenge.SpringRestAPI.entity.CourseGpa;
import com.S17Challenge.SpringRestAPI.exceptions.ApiExceptions;
import com.S17Challenge.SpringRestAPI.validation.CourseValidation;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController // Restful web hizmeti veriyor. HTTP isteklerini dinler, alır ve işler. JSON gibi veri formatlarını alıp işler.
@RequestMapping("/courses") // ana endpoint - url ' i belirliyoruz.
public class CourseController {

    private List<Course> courses; // course listelerini tutmak için bir liste belirliyoruz.

    // courseGPA arayüzünü implement eden üç yapı vardır. Bunlar: lowCourseGpa, mediumCourseGpa, highCourseGpa
    // Bu üç nesne dependency injection(bağımlılık enjeksiyonu) yöntemiyle bu sınıfa enjekte ediliyor.
    private final CourseGpa lowCourseGpa;
    private final CourseGpa mediumCourseGpa;
    private final CourseGpa highCourseGpa;

    // Bu yapı courseController sınıfının construtor'ı yani yapıcı fonksiyonudur.
    // courseController nesnesi oluşturulurken lowCourseGpa,mediumCourseGpa,highCourseGpa nesneleri dışarıdan parametre olarak alınır ve sınıfın ilgili değişkenlerine atanır.
    // dışarıdan enjekte edilen lowCourseGpa  sınıfın içindeki this.lowCouseGpa ya atanır.
    public CourseController(CourseGpa lowCourseGpa, CourseGpa mediumCourseGpa, CourseGpa highCourseGpa) {
        this.lowCourseGpa = lowCourseGpa;
        this.mediumCourseGpa = mediumCourseGpa;
        this.highCourseGpa = highCourseGpa;
    }

    // Bu sınıfın nesnesi ilk kez oluşturulduğunda bu method otomatik olarak çalıştırılacaktır.
    // Eğer elimizdeki sınıfın içinde bir liste var ise onu başlatmak için "postContstruct" anatasyonu kullanmamız şart yoksa "NullPointerException" hatası alırız
    @PostConstruct
    public void init(){

        this.courses = new ArrayList<>();
    }

    @GetMapping
    public List<Course> getAll(){

        return this.courses;
    }


    // name kısmı dinamik bir yapı, istekle gelen dersin adı olur.
    // @PathVariable("name") String name yapısı {name} den alınan değeri "name" e aktarır.
    // courses.stream().filter(c -> c.getName() ---> liste içindeki derslerin adlarını istekle gelen "name" değişkeni ile karşılaştırır.
    // equalsIgnoreCase(name) --> karşılaştırmada büyük/ küçük harf duyarsız şekilde yapar.
    //find.first() --> eşleşen ilk dersi döner. Optional<courses> türünde sonuç döner.
    // Eğer ders bulamaz ise Optional.empty() döner.

    // PathVariable url den veri almak için kullnılır.
    // GET, DELETE gibi istekler de kullanılır.
    // URL deki değerleri alır ve java metoduna aktarır.

    // ResponseEntity yapısını HTTP yanıtlarını ve veriyi dönmesi için kullanırız.
    // Optional<Course> kullanma sebebimiz bir değer olup olmadığını güvenli bir şekilde dönmek için kullanırız ve null döndürme durumlarının önüne geçer.
    @GetMapping("/{name}")
    public ResponseEntity<Course> getCourseByName(@PathVariable("name") String name) {
        Optional<Course> course = courses.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst();

        if (course.isEmpty()) {
            // Burada ApiExceptions fırlatıyoruz.
            throw new ApiExceptions("Course not found", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(course.get());
    }



    // HTTP isteğinin gövdesinden veri almak için kullanılır.
    // POST VE PUT isteklerinde kullanıyoruz.
    // JSON formatlarını java nesnelerine dönüştürmek için kullanılır.

    @PostMapping
    public ResponseEntity<ApiResponse> addCourse(@RequestBody Course course) {
        // Validation işlemleri
        CourseValidation.checkName(course.getName()); // kurs adını kontrol ediyoruz.
        CourseValidation.checkCredit(course.getCredit()); // kredi değerini kontrol ediyoruz.

        // Aynı isimde kurs eklememek için alttaki yapıyı kurduk.
        boolean courseExists = courses.stream()
                .anyMatch(c ->c.getName().equalsIgnoreCase(course.getName()));

        if (courseExists){
            return ResponseEntity.badRequest().body(null); // course zaten var
        }
        if (course.getCredit() < 0 || course.getCredit() > 4){
            return ResponseEntity.badRequest().body(null); // geçersiz kredi değeri
        }


        int totalGpa;
        if (course.getCredit() <=2){
            totalGpa = course.getGrade().getCoefficient()* course.getCredit() * lowCourseGpa.getGpa();
        } else if (course.getCredit() == 3) {
            totalGpa = course.getGrade().getCoefficient() * course.getCredit() * mediumCourseGpa.getGpa();
        } else {
            totalGpa = course.getGrade().getCoefficient()*course.getCredit()* highCourseGpa.getGpa();
        }

        courses.add(course);

        ApiResponse response = new ApiResponse(course,totalGpa);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    ResponseEntity<Course> updateCourse(@PathVariable("id") Integer id,@RequestBody Course newCourse){
        // Validation işlemleri

        CourseValidation.checkId(id); // güncellenecek dersin id'si ni kontrol eder.
        CourseValidation.checkName(newCourse.getName());  // yeni kurs adı kontrolü yapar.
        CourseValidation.checkCredit(newCourse.getCredit()); // yeni kredi değeri kontrol eder.

        Optional<Course> existingCourse = courses.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();

        if (existingCourse.isEmpty()){
            return ResponseEntity.notFound().build(); // Course bulunamadı.
        }

        if (newCourse.getCredit() < 0 || newCourse.getCredit() > 4){
            return ResponseEntity.badRequest().body(null);
        }

        // totalGpa hesaplama
        int totalGpa;
        if (newCourse.getCredit() <= 2) {
            totalGpa = newCourse.getGrade().getCoefficient() * newCourse.getCredit() * lowCourseGpa.getGpa();
        } else if (newCourse.getCredit() == 3) {
            totalGpa = newCourse.getGrade().getCoefficient() * newCourse.getCredit() * mediumCourseGpa.getGpa();
        } else {
            totalGpa = newCourse.getGrade().getCoefficient() * newCourse.getCredit() * highCourseGpa.getGpa();
        }

        existingCourse.get().setName(newCourse.getName());
        existingCourse.get().setCredit(newCourse.getCredit());
        existingCourse.get().setGrade(newCourse.getGrade());

        ApiResponse response = new ApiResponse(existingCourse.get(),totalGpa);
        return ResponseEntity.ok(response.getCourse());
    }

    // build boş yanıt gövdesidir. Sadece HTTP durum kodu döner, veri döndürmez.
    @DeleteMapping("/{id}")
    public ResponseEntity<Course> deleteCourse(@PathVariable("id") Integer id) {
        // Validation işlemleri

        CourseValidation.checkId(id); // Silinecek kursun id'si ni kontrol ediyor.

        boolean removed = courses.removeIf(c -> c.getId().equals(id));

        if (removed){
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
