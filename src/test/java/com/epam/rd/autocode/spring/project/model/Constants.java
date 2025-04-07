package com.epam.rd.autocode.spring.project.model;

public class Constants {
    public static final String CLASS_PACKAGE = "com.epam.rd.autocode.spring.project.model";
    public static final String AGE_GROUP_TYPE = CLASS_PACKAGE + ".enums." + AgeGroup.ENUM_NAME;
    public static final String LANGUAGE_TYPE = CLASS_PACKAGE + ".enums." + Language.ENUM_NAME;
    public static final String BOOK_TYPE = CLASS_PACKAGE + "." + Book.CLASS_NAME;
    public static final String BOOK_ITEM_TYPE = CLASS_PACKAGE + "." + BookItem.CLASS_NAME;
    public static final String CLIENT_TYPE = CLASS_PACKAGE + "." + Client.CLASS_NAME;
    public static final String EMPLOYEE_TYPE = CLASS_PACKAGE + "." + Employee.CLASS_NAME;
    public static final String ORDER_TYPE = CLASS_PACKAGE + "." + Order.CLASS_NAME;
    public static final String USER_TYPE = CLASS_PACKAGE + "." + User.CLASS_NAME;
    public static final String LONG_TYPE = "java.lang.Long";
    public static final String INT_TYPE = "java.lang.Integer";

    public static final String STRING_TYPE = "java.lang.String";
    public static final String BIG_DECIMAL_TYPE = "java.math.BigDecimal";
    public static final String LOCAL_DATE_TYPE = "java.time.LocalDate";
    public static final String LOCAL_DATE_TIME_TYPE = "java.time.LocalDateTime";

    static class AgeGroup {
        public static final String ENUM_NAME = "AgeGroup";
        public static final int ENUM_COUNT_CONSTANTS = 4;
        public static final String ENUM_CONSTANT_CHILD = "CHILD";
        public static final String ENUM_CONSTANT_TEEN = "TEEN";
        public static final String ENUM_CONSTANT_ADULT = "ADULT";
        public static final String ENUM_CONSTANT_OTHER = "OTHER";
    }

    static class Language {
        public static final String ENUM_NAME = "Language";
        public static final int ENUM_COUNT_CONSTANTS = 7;
        public static final String ENUM_CONSTANT_ENGLISH = "ENGLISH";
        public static final String ENUM_CONSTANT_GERMAN = "GERMAN";
        public static final String ENUM_CONSTANT_FRENCH = "FRENCH";
        public static final String ENUM_CONSTANT_SPANISH = "SPANISH";
        public static final String ENUM_CONSTANT_JAPANESE = "JAPANESE";
        public static final String ENUM_CONSTANT_UKRAINIAN = "UKRAINIAN";
        public static final String ENUM_CONSTANT_OTHER = "OTHER";
    }

    static class Book {
        public static final String CLASS_NAME = "Book";
        public static final int CLASS_COUNT_FIELDS = 11;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = CLASS_COUNT_FIELDS;
    }

    static class BookItem {
        public static final String CLASS_NAME = "BookItem";
        public static final int CLASS_COUNT_FIELDS = 4;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = CLASS_COUNT_FIELDS;
    }

    static class Client {
        public static final String CLASS_NAME = "Client";
        public static final int CLASS_COUNT_FIELDS = 1;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = 5;
    }

    static class Employee {
        public static final String CLASS_NAME = "Employee";
        public static final int CLASS_COUNT_FIELDS = 2;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = 6;
    }

    static class Order {
        public static final String CLASS_NAME = "Order";
        public static final int CLASS_COUNT_FIELDS = 6;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = CLASS_COUNT_FIELDS;
    }

    static class User {
        public static final String CLASS_NAME = "User";
        public static final int CLASS_COUNT_FIELDS = 4;
        public static final int CLASS_COUNT_CONSTRUCTORS = 2;
        public static final int PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS = CLASS_COUNT_FIELDS;
    }
}
