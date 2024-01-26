import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test
import ru.samsung.smartintercom.ui.activity.MainActivity


class SimpleTest : TestCase() {

    @get:Rule
    val activityRule = activityScenarioRule<MainActivity>()

    @Test
    fun test() {
        val mDevice =
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        val buttonStart: UiObject = mDevice.findObject(UiSelector().resourceId("ru.samsung.smartintercom:id/button_start"))
        assert(buttonStart.exists())
        buttonStart.click();

        val inputHouseTextInput: UiObject = mDevice.findObject(UiSelector().resourceId("ru.samsung.smartintercom:id/input_house"))
        assert(inputHouseTextInput.exists())
        inputHouseTextInput.setText("22/3");

        val inputFlatTextInput: UiObject = mDevice.findObject(UiSelector().resourceId("ru.samsung.smartintercom:id/input_flat"))
        assert(inputFlatTextInput.exists())
        inputFlatTextInput.setText("13");

        //mDevice.sleep()

        val buttonSave: UiObject = mDevice.findObject(UiSelector().resourceId("ru.samsung.smartintercom:id/button_save"))
        assert(buttonSave.isEnabled())


        buttonSave.click();


        mDevice.pressBack();

        val textIntercomModel: UiObject = mDevice.findObject(UiSelector().resourceId("ru.samsung.smartintercom:id/text_intercom_model"))
        assert(textIntercomModel.exists())

        val buttonRetry: UiObject = mDevice.findObject(UiSelector().resourceId("ru.samsung.smartintercom:id/button_retry"))
        assert(buttonRetry.exists())

        val buttonTakePhoto: UiObject = mDevice.findObject(UiSelector().resourceId("ru.samsung.smartintercom:id/button_take_photo"))
        assert(buttonTakePhoto.exists())

        val buttonSettings: UiObject = mDevice.findObject(UiSelector().resourceId("ru.samsung.smartintercom:id/button_settings"))
        assert(buttonSettings.exists())

        val buttonHistory: UiObject = mDevice.findObject(UiSelector().resourceId("ru.samsung.smartintercom:id/button_history"))
        assert(buttonHistory.exists())


    }
}