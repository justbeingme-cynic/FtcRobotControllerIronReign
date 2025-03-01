package org.firstinspires.ftc.teamcode.robots.csbot.subsystem;

import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.robots.csbot.util.Joint;
import static org.firstinspires.ftc.teamcode.util.utilMethods.futureTime;
import org.firstinspires.ftc.teamcode.robots.csbot.util.Utils;

import java.util.LinkedHashMap;
import java.util.Map;

//i still have no clue what the intake needs lmao

@Config(value = "CS_INTAKE")
public class Intake implements Subsystem {
    public static  int ANGLE_CONTROLLER_MAX = 1800;
    public static int ANGLE_CONTROLLER_MIN = 850;
    //CONSTANTS
    HardwareMap hardwareMap;
    Robot robot;
    Servo diverter;
    Servo angleController;
    Joint beaterBarAngleController;
    DcMotorEx beaterBar;
    public static boolean precisionBeaterBar = false;
    public static boolean manualBeaterBarEject = false;
    public static boolean manualBeaterBarOn = false;
    public static double BEATER_BAR_ADJUST_SPEED = 2;
    public static double BEATER_BAR_INTAKE_VELOCITY = 1500;
    public static double BEATER_BAR_EJECT_VELOCITY = -700;
    public static int BEATER_BAR_ANGLE_CONTROLLER_HOME = 1350;
    public static double BEATER_BAR_ANGLE_CONTROLLER_TICKS_PER_DEGREE;
    public static double BEATER_BAR_ANGLE_CONTROLLER_MIN_DEGREES;
    public static double BEATER_BAR_ANGLE_CONTROLLER_MAX_DEGREES;
    public static double BEATER_BAR_ANGLE_CONTROLLER_START_ANGLE;
    public static double BEATER_BAR_ANGLE_CONTROLLER_SPEED;
    public static int angleControllerTicks = 1000;
    public static double BEATER_BAR_FOLD_ANGLE;
    public static double BEATER_BAR_GROUND_ANGLE = 1045;


    public enum Articulation {
        WING_INTAKE_POSTION,
        STACK_INTAKE,
        EJECT,
        OFF,
        FOLD,
        MANUAL,
    }


    //LIVE STATES
    public Articulation articulation;
    public static int numPixelsInStack = 6;
    public static double angleToStack;
    public static double beaterBarTargetAngle;


    public Intake(HardwareMap hardwareMap, Robot robot) {
            this.hardwareMap = hardwareMap;
            this.robot = robot;
            articulation = Articulation.MANUAL;

            diverter = hardwareMap.get(Servo.class, "diverter");
            beaterBar = hardwareMap.get(DcMotorEx.class, "beaterBar");
            beaterBar.setDirection(DcMotorSimple.Direction.REVERSE);
            angleController = hardwareMap.get(Servo.class, "beaterBarAngleController");

//            beaterBarAngleController = new Joint(hardwareMap, "beaterBarAngleController", false, BEATER_BAR_ANGLE_CONTROLLER_HOME, BEATER_BAR_ANGLE_CONTROLLER_TICKS_PER_DEGREE, BEATER_BAR_ANGLE_CONTROLLER_MIN_DEGREES, BEATER_BAR_ANGLE_CONTROLLER_MAX_DEGREES, BEATER_BAR_ANGLE_CONTROLLER_START_ANGLE, BEATER_BAR_ANGLE_CONTROLLER_SPEED);
            beaterBar.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }
    public void update(){
        update(new Canvas());
    }

    @Override
    public void update(Canvas fieldOverlay) {
        switch (articulation) {
            case MANUAL:
                if(manualBeaterBarOn) {
                    if(!manualBeaterBarEject) {
                        beaterBar.setPower(1);
                        beaterBar.setVelocity(BEATER_BAR_INTAKE_VELOCITY);
                    }
                    else {
                        beaterBar.setPower(1);
                        beaterBar.setVelocity(BEATER_BAR_EJECT_VELOCITY);
                    }
                }
                else
                    beaterBar.setVelocity(0);
                break;
            case OFF:
                beaterBar.setPower(0);
                break;
            case WING_INTAKE_POSTION:
                if(wingIntakePostion()) {
                    articulation = Articulation.MANUAL;
                }
                break;
            case EJECT:
                beaterBar.setVelocity(BEATER_BAR_EJECT_VELOCITY);
                break;
            case FOLD:
                beaterBar.setPower(0);
                beaterBarTargetAngle = BEATER_BAR_FOLD_ANGLE;
                break;
            case STACK_INTAKE:
                beaterBar.setPower(BEATER_BAR_INTAKE_VELOCITY);
                beaterBarTargetAngle = angleToStack;
                break;
        }
        angleController.setPosition(Utils.servoNormalize(angleControllerTicks));
//        beaterBarAngleController.setTargetAngle(beaterBarTargetAngle);
//        beaterBarAngleController.update();
    }

    public Articulation articulate(Articulation target) {
        articulation = target;
        return articulation;
    }


    public boolean wingIntakePostion (){
        angleController.setPosition(1011);
        beaterBar.setVelocity(BEATER_BAR_INTAKE_VELOCITY);
        return true;
    }

    public void togglePrecisionBeaterBar() {
        precisionBeaterBar = !precisionBeaterBar;
    }

    public double adjustBeaterBarAngle(double speed) {
        angleControllerTicks += speed * (precisionBeaterBar ? 10 : 100);
        if(angleControllerTicks < ANGLE_CONTROLLER_MIN) {
            angleControllerTicks = ANGLE_CONTROLLER_MIN;
        }
        if(angleControllerTicks > ANGLE_CONTROLLER_MAX) {
            angleControllerTicks = ANGLE_CONTROLLER_MAX;
        }

        angleController.setPosition(
        Utils.servoNormalize(angleControllerTicks));
//        beaterBarTargetAngle += speed * BEATER_BAR_ADJUST_SPEED;
        return angleControllerTicks;
    }
    public void BeaterBarUp(boolean beaterBarUp)
    {
        if(beaterBarUp)
            angleControllerTicks = ANGLE_CONTROLLER_MAX;
        else
           angleControllerTicks = BEATER_BAR_ANGLE_CONTROLLER_HOME;
    }

    public void toggleBeaterBar() {
        manualBeaterBarOn = !manualBeaterBarOn;
    }
    public void switchBeaterBarDirection (){
        manualBeaterBarEject = !manualBeaterBarEject;
    }

    @Override
    public void stop() {
        beaterBar.setVelocity(0);
    }

    @Override
    public Map<String, Object> getTelemetry(boolean debug) {
        Map<String, Object> telemetryMap = new LinkedHashMap<>();
        telemetryMap.put("articulation", articulation.name());
        telemetryMap.put("manual beater bar on?", manualBeaterBarOn);
        telemetryMap.put("beater bar amps", beaterBar.getPower());
        telemetryMap.put("angle controller position", Utils.servoDenormalize(angleController.getPosition()));
//        telemetryMap.put("beaterBarAngle", beaterBarAngleController.getCurrentAngle());
        return telemetryMap;
    }

    @Override
    public String getTelemetryName() {
        return "INTAKE";
    }
}
