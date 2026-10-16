package io.runtime.mcumgr.dfu.mcuboot;

import io.runtime.mcumgr.McuMgrTransport;
import io.runtime.mcumgr.dfu.FirmwareUpgradeCallback;

/**
 * Compatibility bridge for Veepoo SDK OTA manager.
 * Forwards calls to io.runtime.mcumgr.dfu.FirmwareUpgradeManager.
 */
public class FirmwareUpgradeManager extends io.runtime.mcumgr.dfu.FirmwareUpgradeManager {

    public FirmwareUpgradeManager(McuMgrTransport transport) {
        super(transport);
    }

    public FirmwareUpgradeManager(McuMgrTransport transport, FirmwareUpgradeCallback callback) {
        super(transport, callback);
    }
}
