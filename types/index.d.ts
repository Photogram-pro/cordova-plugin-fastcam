interface ResultingFile {
  filePath: string;
  timestamp: number;
  fileType: "VIDEO" | "IMAGE";
}

interface StartCameraParams {
  /**
   * Defaults to SINGLE_PHOTO
   */
  mode?: "PHOTO_SERIES" | "SINGLE_PHOTO" | "VIDEO";
  /**
   * Before taking photos or videos,
   * this timestamp can be set from
   * the outside to mark an arbitrary
   * point in time of another system.
   * The camera class will, from that point
   * on, start a timer and use this
   * time as a frame of reference for
   * video and picture timestamps.
   * This can be used to sync with
   * external devices like GPS.
   */
  clockSyncTimestamp?: number;
}

interface FastCamera {
  startCamera(p: StartCameraParams): Promise<ResultingFile[]>;
}

interface Navigator {
  fastCamera: FastCamera;
}

declare module "cordova-plugin-fastcam" {}
