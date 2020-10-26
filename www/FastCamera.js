const exec = require("cordova/exec");

class FastCamera {
  static PLUGIN_NAME = "FastCamera";

  static startCamera({ mode = "SINGLE_PHOTO", clockSyncTimestamp = 0 } = {}) {
    return new Promise((resolve, reject) => {
      exec(
        (res) => {
          const parsed = JSON.parse(res);
          resolve(parsed);
        },
        reject,
        FastCamera.PLUGIN_NAME,
        "startCamera",
        [mode, clockSyncTimestamp]
      );
    });
  }
}

navigator.fastCamera = FastCamera;
