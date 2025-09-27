// แก้ไขชื่อโมดูลให้ตรงกับ package หลัก
module se233.inverted.chapter3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.pdfbox; // require แค่นี้พอครับ ไม่ต้องมี .io

    // แก้ไขทุกบรรทัดให้มี .inverted
    opens se233.inverted.chapter3 to javafx.fxml;
    opens se233.inverted.chapter3.controller to javafx.fxml;
    exports se233.inverted.chapter3.controller;
    exports se233.inverted.chapter3;
}