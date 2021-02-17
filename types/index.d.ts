declare enum GpsFixType {
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
  /**
   * Altitude above geoid
   * with interpolated
   * geoid height from
   * custom grid files
   */
  altitude: number;
  /**
   * Interpolated  geoid
   * height
   */
  interpolatedGeoid: number;
  /**
   * Original geoid height
   */
  geoidH: number;
  /**
   * Original altitude
   */
  origAltitude: number;
  dir: number;
  fixed: boolean;
  lat: number;
  lon: number;
  quality: GpsFixType;
  time: number;
  velocity: number;
}

/**
 * An array of this type
 * is returned from
 * the 'startCamera' method
 */
interface ResultingFile {
  filePath: string;
  /**
   * For photos, this is
   * the exact timestamp the
   * image was taken, and for
   * videos, it's the timestamp
   * when the camera finished
   * capturing.
   */
  timestamp: number;
  fileType: "VIDEO" | "IMAGE";
  /**
   * If a GPS device was connected
   * using USB OTG, the position
   * will be saved here.
   */
  position?: GpsPosition;
}

interface StartCameraParams {
  /**
   * Defaults to SINGLE_PHOTO.
   * - SINGLE_PHOTO = Take one picture and close the camera
   * - PHOTO_SERIES = Take a series of photos with a rate of 200ms and close the camera
   * - VIDEO = Take a video and close the camera
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
  /**
   * Antenna mount altitude in CM
   */
  altitudeDifference?: number;
  onData: (position: GpsPosition) => void;
  onError?: (e: any) => void;
}

interface FastCamera {
  startCamera(p: StartCameraParams): Promise<ResultingFile[]>;
  initGps(p: InitGpsParams): void;
  simulateGps(p: InitGpsParams): void;
}

interface Navigator {
  fastCamera: FastCamera;
}

declare module "cordova-plugin-fastcam" {}
