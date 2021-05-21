package workflow.task

import krews.core.WorkflowBuilder
import krews.core.*
import krews.file.File
import krews.file.OutputFile
import workflow.model.*
import org.reactivestreams.Publisher


data class HotSpotParams(
    val chromSizeFile: File,
    val cores: Int = 8,
    val siteFile: File? = null,
    val candidateRegionFile: File? = null,
    val mappableRegFile: File? = null,
    val neighborhoodSize: Int? = 100,
    val windowSize: Int? = 25000,
    val minHotspotWidth: Int? = 50,
    val writePvalue: Boolean = true,
    val hotspotThresh: Double? = 0.05,
    val sitecallThresh: Double? = 0.05,
    val smoothingParam: Int? = 5,
    val peaksDefinition: String? = "default_peaks"
)

data class HotSpotInput(
    val bamRep: Replicate
)

data class HotSpotOutput(
    val starchFile: File,
    val bigWigFile: File,
    val allcallsstarchFile: File,
    val bamFile: File,
    val sitesFile: File,
    val repName: String
)

fun WorkflowBuilder.HotspotTask(name: String, i: Publisher<HotSpotInput>)
  = this.task<HotSpotInput,  HotSpotOutput>(name, i) {
    
    val params = taskParams<HotSpotParams>()
    dockerImage = "genomealmanac/dnaseseq-hotspot:v1.0.8"
    val prefix = "${input.bamRep.name}"

    output = HotSpotOutput(
            repName = input.bamRep.name,
            allcallsstarchFile = OutputFile("nuclear.${prefix}.allcalls.starch"),
            bigWigFile = OutputFile("nuclear.${prefix}.density.bw"),
            starchFile = OutputFile("nuclear.${prefix}.density.starch"),
            bamFile = OutputFile("nuclear.${prefix}.bam"),
            sitesFile = OutputFile("generatedcentersitesfile.starch")
    )
    val bamRep = input.bamRep


    command = if (bamRep is BamReplicate) """
        export TMPDIR="${outputsDir}"
        java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 \
            -jar /app/hotspot.jar \
                --bam ${bamRep.bamFile.dockerPath} \
                --chrom-size ${params.chromSizeFile.dockerPath} \
                --output-directory ${outputsDir} \
                --peaks-def ${params.peaksDefinition} \
                --sitecall-thresh ${params.sitecallThresh} \
                --smoothing-param ${params.smoothingParam} \
                --hotspot-thresh ${params.hotspotThresh} \
                --number-of-cores ${params.cores} \
                ${if (params.writePvalue) "--write-pvalue" else ""} \
                --min-hotspot-width ${params.minHotspotWidth} \
                --window-size ${params.windowSize} \
                --neighborhood-size ${params.neighborhoodSize} \
                ${ if (params.candidateRegionFile !== null) "--candidate-region ${params.candidateRegionFile.dockerPath}" else "" } \
                ${ if (params.siteFile !== null) "--sites ${params.siteFile.dockerPath}" else "" } \
                ${ if (params.mappableRegFile !== null) "--mappable-reg ${params.mappableRegFile.dockerPath}" else "" }

    """ else ""

}
