package workflow.task

import krews.core.WorkflowBuilder
import krews.core.*
import krews.file.File
import krews.file.OutputFile
import workflow.model.*
import org.reactivestreams.Publisher


data class NormalizeBigWigParameters(
    val faiFile: File,
    val cores: Int = 8
)

data class NormalizeBigWigInput(
    val bamFile: File,
    val densityStarchFile: File,
    val name: String
)

data class NormalizeBigWigOutput(
    val normalizedStarchFile: File,
    val normalizedBigWigFile: File
)

fun WorkflowBuilder.NormalizeBigWigTask(name: String, i: Publisher<NormalizeBigWigInput>)
  = this.task<NormalizeBigWigInput,  NormalizeBigWigOutput>(name, i) {
    
    val params = taskParams<NormalizeBigWigParameters>()
    dockerImage = "genomealmanac/dnaseseq-normalize-bigwig:v1.0.6"
    val prefix = "${input.name}"

    output = NormalizeBigWigOutput(
        normalizedStarchFile = OutputFile("normalized.starched.${prefix}.starch"),
        normalizedBigWigFile = OutputFile("normalized.${prefix}.bigwig")
    )


    command = """
        export TMPDIR="${outputsDir}"
        java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 \
            -jar /app/normalizebigwig.jar \
                --bam-file ${input.bamFile.dockerPath} \
                --density-starch-file ${input.densityStarchFile.dockerPath} \
                --fa-index ${params.faiFile.dockerPath} \
                --output-directory ${outputsDir} \
                --output-prefix ${input.name} \
                --number-of-cores ${params.cores}
        """

}
