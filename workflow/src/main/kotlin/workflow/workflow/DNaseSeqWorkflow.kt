package workflow.workflow

import krews.core.*
import krews.run
import reactor.core.publisher.toFlux

import workflow.model.*
import workflow.task.*

data class DNaseSeqParams(
    val experiments: List<Experiment>
)
val dnaseSeqWorkflow = workflow("dnaseseq-workflow") {

    val params = params<DNaseSeqParams>()

    /* create hotspot task for DNase seq alignments */
    val hotspotInput = params.experiments.flatMap {
        it.replicates
                .filter { it is BamReplicate }
            .map { HotSpotInput(it) }
    }.toFlux()

    val hotspotTask = HotspotTask("hotspot", hotspotInput)
    val normalizeBigWigInput =  hotspotTask.map { NormalizeBigWigInput(it.bamFile, it.starchFile, it.repName) }

    NormalizeBigWigTask("normalizebigwig", normalizeBigWigInput)
}
