package workflow.model

import krews.file.File

data class BamReplicate (
    override val name: String,
    val bamFile: File
): Replicate
