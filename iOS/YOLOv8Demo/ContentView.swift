import SwiftUI

import ZeticMLange
import ext

struct ContentView: View {
    @StateObject var cameraSource: CameraSource
    @StateObject var pipeline: ZeticMLangePipeline<CameraFrame, ObjectDetectionResult>
    
    init() {
        let camera = CameraSource()
        let yamlURL = Bundle.main.url(forResource: "coco", withExtension: "yaml")!
        let detection = ObjectDetection(cocoYamlPath: yamlURL.absoluteString)
        
        _pipeline = StateObject(wrappedValue: ZeticMLangePipeline(feature: detection, inputSource: camera))
        _cameraSource = StateObject(wrappedValue: camera)
    }
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                if let previewLayer = cameraSource.previewLayer {
                    CameraPreviewView(previewLayer: previewLayer)
                }
                
                if let detection = pipeline.latestResult {
                    DetectionsView(detectionResult: detection, cameraResolution: cameraSource.resolution)
                }
            }
        }
        .onAppear {
            pipeline.startLoop()
        }
        .onDisappear {
            pipeline.stopLoop()
        }
    }
}
