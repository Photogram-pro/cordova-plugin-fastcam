export enum GpsFixType {
  INVALID = 0,
  GPS = 1,
  DGPS = 2,
  PPS = 3,
  RTK_FIXED = 4,
  FLOAT_RTK = 5,
  ESTIMATED = 6,
  MANUAL = 7,
  SIMULATION = 8,
}

interface GpsPosition {
  altitude: number;
  dir: number;
  fixed: boolean;
  lat: number;
  lon: number;
  quality: GpsFixType;
  time: number;
  velocity: number;
}

interface ResultingFile {
  filePath: string;
  timestamp: number;
  fileType: "VIDEO" | "IMAGE";
  position?: GpsPosition;
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

interface InitGpsParams {
  baudRate?: number;
  onData: (position: GpsPosition) => void;
  onError?: (e: any) => void;
}

interface FastCamera {
  startCamera(p: StartCameraParams): Promise<ResultingFile[]>;
  initGps(p: InitGpsParams): void;
}

interface Navigator {
  fastCamera: FastCamera;
}

declare module "cordova-plugin-fastcam" {}
