package io.hhplus.tdd.point.exception;

public class PointException extends RuntimeException {
    public static final String ERR_NOT_FOUND = "NOT_FOUND"; //사용자 포인트 없음
    public static final String ERR_MAX = "MAX_POINTS";      //최대 허용 포인트 초과
    public static final String ERR_LOW = "LOW_POINTS";      //사용 가능한 포인트 부족
    public static final String ERR_INV = "INVALID_OP";      //잘못된 연산

    private final String type; //예외 유형

    //예외 객체 정의
    public static final PointException EX_NOT_FOUND = new PointException(ERR_NOT_FOUND, "사용자의 포인트가 존재하지 않습니다.");
    public static final PointException EX_MAX = new PointException(ERR_MAX, "최대 허용 포인트를 초과했습니다.");
    public static final PointException EX_LOW = new PointException(ERR_LOW, "사용 가능한 포인트가 부족합니다.");
    public static final PointException EX_INV = new PointException(ERR_INV, "잘못된 포인트 연산입니다.");

    public PointException(String type, String message) {
        super(message);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
