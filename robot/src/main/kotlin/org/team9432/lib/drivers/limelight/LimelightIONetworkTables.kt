package org.team9432.lib.drivers.limelight

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import edu.wpi.first.math.geometry.*
import edu.wpi.first.math.util.Units
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.networktables.NetworkTableInstance
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture

// basically LimelightLib, but kotlin!
@Suppress("unused")
class LimelightIONetworkTables(private val tableName: String = "limelight"): LimelightIO {

    override fun updateInputs(inputs: LimelightIO.LimelightIOInputs) {
        inputs.cameraPose = cameraPose3dTargetSpace
        inputs.tagID = fiducialID

        val xCorners: MutableList<Double> = mutableListOf()
        val yCorners: MutableList<Double> = mutableListOf()
        val tags = getLatestResults().targetingResults.targetsFiducials
        for (tag in tags) {
            if (tag?.corners == null) continue
            for (corner in tag.corners) {
                if (corner == null) continue
                xCorners.add(corner[0])
                yCorners.add(corner[1])
            }
        }
        inputs.xCorners = DoubleArray(xCorners.size) { index -> xCorners[index] }
        inputs.yCorners = DoubleArray(yCorners.size) { index -> yCorners[index] }
    }

    private var mapper: ObjectMapper? = null

    /** Print JSON Parse time to the console in milliseconds */
    private var profileJSON = false
    val disablePrints = true

    val limelightNTTable: NetworkTable get() = NetworkTableInstance.getDefault().getTable(tableName)

    private fun getTableEntry(entryName: String): NetworkTableEntry {
        return limelightNTTable.getEntry(entryName)
    }

    private fun getDouble(entryName: String): Double {
        return getTableEntry(entryName).getDouble(0.0)
    }

    private fun setDouble(entryName: String, value: Double) {
        getTableEntry(entryName).setDouble(value)
    }

    private fun setDoubleArray(entryName: String, value: Array<Double>) {
        getTableEntry(entryName).setDoubleArray(value)
    }

    private fun getDoubleArray(entryName: String): DoubleArray {
        return getTableEntry(entryName).getDoubleArray(DoubleArray(0))
    }

    private fun getString(entryName: String): String {
        return getTableEntry(entryName).getString("")
    }

    private fun getLimelightURLString(request: String) = URL("http://$tableName.local:5807/$request")


    val tx get() = getDouble("tx")
    val ty get() = getDouble("ty")
    val ta get() = getDouble("ta")
    val latencyPipeline get() = getDouble("tl")
    val latencyCapture get() = getDouble("cl")
    val currentPipelineIndex get() = getDouble("getpipe")
    val jsonDump get() = getString("json")

    val botPose get() = getDoubleArray("botpose")
    val botPoseWPIRed get() = getDoubleArray("botpose_wpired")
    val botPoseWPIBlue get() = getDoubleArray("botpose_wpiblue")
    val botPoseTargetSpace get() = getDoubleArray("botpose_targetspace")
    val cameraPoseTargetSpace get() = getDoubleArray("camerapose_targetspace")
    val targetPoseCameraSpace get() = getDoubleArray("targetpose_cameraspace")
    val targetPoseRobotSpace get() = getDoubleArray("targetpose_robotspace")
    val targetColor get() = getDoubleArray("tc")
    val fiducialID get() = getDouble("tid")
    val neuralClassID get() = getDouble("tclass")

    val botPose3d get() = toPose3D(getDoubleArray("botpose"))
    val botPose3dWPIRed get() = toPose3D(getDoubleArray("botpose_wpired"))
    val botPose3dWPIBlue get() = toPose3D(getDoubleArray("botpose_wpiblue"))
    val botPose3dTargetSpace get() = toPose3D(getDoubleArray("botpose_targetspace"))
    val cameraPose3dTargetSpace get() = toPose3D(getDoubleArray("camerapose_targetspace"))
    val targetPose3dCameraSpace get() = toPose3D(getDoubleArray("targetpose_cameraspace"))
    val targetPose3dRobotSpace get() = toPose3D(getDoubleArray("targetpose_robotspace"))
    val cameraPose3dRobotSpace get() = toPose3D(getDoubleArray("camerapose_robotspace"))


    val botPose2dWPIBlue get() = toPose2D(botPoseWPIBlue)
    val botPose2dWPIRed get() = toPose2D(botPoseWPIRed)
    val botPose2d get() = toPose2D(botPose)

    val tv get() = 1.0 == getDouble("tv")

    fun setPipelineIndex(pipelineIndex: Int) {
        setDouble("pipeline", pipelineIndex.toDouble())
    }

    override fun setLEDMode(ledMode: LEDMode) = setDouble("ledMode", ledMode.value)

    enum class LEDMode(val value: Double) {
        PIPELINE_CONTROL(0.0), FORCE_OFF(1.0), FORCE_BLINK(2.0), FORCE_ON(3.0)
    }

    fun setStreamMode(streamMode: StreamMode) {
        setDouble("stream", streamMode.value)
    }

    enum class StreamMode(val value: Double) {
        STANDARD(0.0), PIP_MAIN(1.0), PIP_SECONDARY(2.0)
    }

    fun setCameraMode(cameraMode: CameraMode) {
        setDouble("camMode", cameraMode.value)
    }

    enum class CameraMode(val value: Double) {
        PROCESSOR(0.0), DRIVER(1.0)
    }

    fun setCropWindow(cropXMin: Double, cropXMax: Double, cropYMin: Double, cropYMax: Double) {
        val entries = arrayOf(cropXMin, cropXMax, cropYMin, cropYMax)
        setDoubleArray("crop", entries)
    }

    fun setCameraPoseRobotSpace(forward: Double, side: Double, up: Double, roll: Double, pitch: Double, yaw: Double) {
        val entries = arrayOf(forward, side, up, roll, pitch, yaw)
        setDoubleArray("camerapose_robotspace_set", entries)
    }


    fun setPythonScriptData(outgoingPythonData: Array<Double>) {
        setDoubleArray("llrobot", outgoingPythonData)
    }

    fun getPythonScriptData(): DoubleArray {
        return getDoubleArray("llpython")
    }


    /**
     * Asynchronously take snapshot.
     */
    fun takeSnapshot(snapshotName: String?): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync { synchTakeSnapshot(snapshotName) }
    }

    private fun synchTakeSnapshot(snapshotName: String?): Boolean {
        val url = getLimelightURLString("capturesnapshot")
        try {
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestMethod("GET")
            if (snapshotName != null && snapshotName !== "") {
                connection.setRequestProperty("snapname", snapshotName)
            }
            val responseCode = connection.getResponseCode()
            if (responseCode == 200) {
                return true
            } else {
                if (!disablePrints) System.err.println("Bad LL Request")
            }
        } catch (e: IOException) {
            if (!disablePrints) System.err.println(e.message)
        }
        return false
    }

    /**
     * Parses Limelight's JSON results dump into a LimelightResults Object
     */
    fun getLatestResults(): LimelightResults {
        val start = System.nanoTime()
        var results = LimelightResults()
        if (mapper == null) {
            mapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
        val json = jsonDump
        if (json.isNotEmpty()) {
            try {
                results = mapper!!.readValue(jsonDump, LimelightResults::class.java)
            } catch (e: JsonProcessingException) {
                if (!disablePrints) System.err.println("lljson error: " + e.message)
            }
        }
        val end = System.nanoTime()
        val millis = (end - start) * .000001
        results.targetingResults.latencyJsonParse = millis
        if (profileJSON) {
            if (!disablePrints) println("lljson: $millis")
        }
        return results
    }

    class LimelightTargetRetro {
        @JsonProperty("t6c_ts")
        private val cameraPoseTargetSpace = DoubleArray(6)

        @JsonProperty("t6r_fs")
        private val robotPoseFieldSpace = DoubleArray(6)

        @JsonProperty("t6r_ts")
        private val robotPoseTargetSpace = DoubleArray(6)

        @JsonProperty("t6t_cs")
        private val targetPoseCameraSpace = DoubleArray(6)

        @JsonProperty("t6t_rs")
        private val targetPoseRobotSpace = DoubleArray(6)

        fun getCameraPoseTargetSpace(): Pose3d {
            return toPose3D(cameraPoseTargetSpace)
        }

        fun getRobotPoseFieldSpace(): Pose3d {
            return toPose3D(robotPoseFieldSpace)
        }

        fun getRobotPoseTargetSpace(): Pose3d {
            return toPose3D(robotPoseTargetSpace)
        }

        fun getTargetPoseCameraSpace(): Pose3d {
            return toPose3D(targetPoseCameraSpace)
        }

        fun getTargetPoseRobotSpace(): Pose3d {
            return toPose3D(targetPoseRobotSpace)
        }

        val cameraPoseTargetSpace2D: Pose2d
            get() = toPose2D(cameraPoseTargetSpace)
        val robotPoseFieldSpace2D: Pose2d
            get() = toPose2D(robotPoseFieldSpace)
        val robotPoseTargetSpace2D: Pose2d
            get() = toPose2D(robotPoseTargetSpace)
        val targetPoseCameraSpace2D: Pose2d
            get() = toPose2D(targetPoseCameraSpace)
        val targetPoseRobotSpace2D: Pose2d
            get() = toPose2D(targetPoseRobotSpace)

        @JsonProperty("ta")
        var ta = 0.0

        @JsonProperty("tx")
        var tx = 0.0

        @JsonProperty("txp")
        var txPixels = 0.0

        @JsonProperty("ty")
        var ty = 0.0

        @JsonProperty("typ")
        var tyPixels = 0.0

        @JsonProperty("ts")
        var ts = 0.0

    }

    class LimelightTargetFiducial {
        @JsonProperty("fID")
        var fiducialID = 0.0

        @JsonProperty("fam")
        var fiducialFamily: String? = null

        @JsonProperty("t6c_ts")
        private val cameraPoseTargetSpace: DoubleArray = DoubleArray(6)

        @JsonProperty("t6r_fs")
        private val robotPoseFieldSpace: DoubleArray = DoubleArray(6)

        @JsonProperty("t6r_ts")
        private val robotPoseTargetSpace: DoubleArray = DoubleArray(6)

        @JsonProperty("t6t_cs")
        private val targetPoseCameraSpace: DoubleArray = DoubleArray(6)

        @JsonProperty("t6t_rs")
        private val targetPoseRobotSpace: DoubleArray = DoubleArray(6)


        fun getCameraPoseTargetSpace(): Pose3d {
            return toPose3D(cameraPoseTargetSpace)
        }

        fun getRobotPoseFieldSpace(): Pose3d {
            return toPose3D(robotPoseFieldSpace)
        }

        fun getRobotPoseTargetSpace(): Pose3d {
            return toPose3D(robotPoseTargetSpace)
        }

        fun getTargetPoseCameraSpace(): Pose3d {
            return toPose3D(targetPoseCameraSpace)
        }

        fun getTargetPoseRobotSpace(): Pose3d {
            return toPose3D(targetPoseRobotSpace)
        }

        val cameraPoseTargetSpace2D: Pose2d
            get() = toPose2D(cameraPoseTargetSpace)
        val robotPoseFieldSpace2D: Pose2d
            get() = toPose2D(robotPoseFieldSpace)
        val robotPoseTargetSpace2D: Pose2d
            get() = toPose2D(robotPoseTargetSpace)
        val targetPoseCameraSpace2D: Pose2d
            get() = toPose2D(targetPoseCameraSpace)
        val targetPoseRobotSpace2D: Pose2d
            get() = toPose2D(targetPoseRobotSpace)

        @JsonProperty("pts")
        var corners: Array<Array<Double>?> = arrayOfNulls(0)

        @JsonProperty("ta")
        var ta = 0.0

        @JsonProperty("tx")
        var tx = 0.0

        @JsonProperty("txp")
        var txPixels = 0.0

        @JsonProperty("ty")
        var ty = 0.0

        @JsonProperty("typ")
        var tyPixels = 0.0

        @JsonProperty("ts")
        var ts = 0.0

    }

    class LimelightTargetBarcode
    class LimelightTargetClassifier {
        @JsonProperty("class")
        var className: String? = null

        @JsonProperty("classID")
        var classID = 0.0

        @JsonProperty("conf")
        var confidence = 0.0

        @JsonProperty("zone")
        var zone = 0.0

        @JsonProperty("tx")
        var tx = 0.0

        @JsonProperty("txp")
        var txPixels = 0.0

        @JsonProperty("ty")
        var ty = 0.0

        @JsonProperty("typ")
        var tyPixels = 0.0
    }

    class LimelightTargetDetector {
        @JsonProperty("class")
        var className: String? = null

        @JsonProperty("classID")
        var classID = 0.0

        @JsonProperty("conf")
        var confidence = 0.0

        @JsonProperty("ta")
        var ta = 0.0

        @JsonProperty("tx")
        var tx = 0.0

        @JsonProperty("txp")
        var txPixels = 0.0

        @JsonProperty("ty")
        var ty = 0.0

        @JsonProperty("typ")
        var tyPixels = 0.0
    }

    class Results {
        @JsonProperty("pID")
        var pipelineID = 0.0

        @JsonProperty("tl")
        var latencyPipeline = 0.0

        @JsonProperty("cl")
        var latencyCapture = 0.0
        var latencyJsonParse = 0.0

        @JsonProperty("ts")
        var timestampLimelightPublish = 0.0

        @JsonProperty("ts_rio")
        var timestampRiofpgaCapture = 0.0

        @JsonProperty("v")
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        var valid = false

        @JsonProperty("botpose")
        var botpose: DoubleArray = DoubleArray(6)

        @JsonProperty("botpose_wpired")
        var botposeWPIRed: DoubleArray = DoubleArray(6)

        @JsonProperty("botpose_wpiblue")
        var botposeWPIBlue: DoubleArray = DoubleArray(6)

        @JsonProperty("t6c_rs")
        var cameraposeRobotspace: DoubleArray = DoubleArray(6)
        val botPose3d: Pose3d
            get() = toPose3D(botpose)
        val botPose3dWPIRed: Pose3d
            get() = toPose3D(botposeWPIRed)
        val botPose3dWPIBlue: Pose3d
            get() = toPose3D(botposeWPIBlue)
        val botPose2d: Pose2d
            get() = toPose2D(botpose)
        val botPose2dWPIRed: Pose2d
            get() = toPose2D(botposeWPIRed)
        val botPose2dWPIBlue: Pose2d
            get() = toPose2D(botposeWPIBlue)

        @JsonProperty("Retro")
        var targetsRetro: Array<LimelightTargetRetro?> = arrayOfNulls(0)

        @JsonProperty("Fiducial")
        var targetsFiducials: Array<LimelightTargetFiducial?> = arrayOfNulls(0)

        @JsonProperty("Classifier")
        var targetsClassifier: Array<LimelightTargetClassifier?> = arrayOfNulls(0)

        @JsonProperty("Detector")
        var targetsDetector: Array<LimelightTargetDetector?> = arrayOfNulls(0)

        @JsonProperty("Barcode")
        var targetsBarcode: Array<LimelightTargetBarcode?> = arrayOfNulls(0)

    }

    class LimelightResults {
        @JsonProperty("Results")
        var targetingResults: Results = Results()
    }
}

private fun toPose3D(inData: DoubleArray): Pose3d {
    if (inData.size < 6) {
        return Pose3d()
    }
    return Pose3d(
        Translation3d(inData[0], inData[1], inData[2]),
        Rotation3d(
            Units.degreesToRadians(inData[3]), Units.degreesToRadians(inData[4]),
            Units.degreesToRadians(inData[5])
        )
    )
}

private fun toPose2D(inData: DoubleArray): Pose2d {
    if (inData.size < 6) {
        return Pose2d()
    }
    val tran2d = Translation2d(inData[0], inData[1])
    val r2d = Rotation2d(Units.degreesToRadians(inData[5]))
    return Pose2d(tran2d, r2d)
}