
library(Signac)
library(Seurat)

################################################################################
################### Load data and metadata - as usual ##########################
################################################################################


# load the RNA and ATAC data -- aggregated data from cellranger (according to libs.csv)
counts <- Read10X_h5("/corgi/cellbuster/10xpbmc/aggr/outs/filtered_feature_bc_matrix.h5")
fragpath <- "/corgi/cellbuster/10xpbmc/aggr/outs/atac_fragments.tsv.gz"

# get gene annotations for hg38
annotation <- GetGRangesFromEnsDb(ensdb = EnsDb.Hsapiens.v86)
seqlevelsStyle(annotation) <- "UCSC"

# create a Seurat object containing the RNA adata
adata <- CreateSeuratObject(
  counts = counts$`Gene Expression`,
  assay = "RNA"
)

# create ATAC assay and add it to the object
adata[["peaks"]] <- CreateChromatinAssay(
  counts = counts$Peaks,
  sep = c(":", "-"),
  fragments = fragpath,
  annotation = annotation
)

## Crucial - figure out which barcode comes from which library (aggregated data)
adata$lib <- str_split_fixed(colnames(adata),"-",2)[,2]

remove(counts)


## Cellranger, on aggregated data, renames barcodes according the list in libs.csv (the input list of datasets).
## The mapping here is to help map these names to the original library names. Optionally, one could read a parser
## for the libs.csv input file
map_ds2i <- data.frame(row.names=c(
  "10k_PBMC_Multiome_nextgem_Chromium_Controller",
  "10k_PBMC_Multiome_nextgem_Chromium_X",
  "pbmc_granulocyte_sorted_10k",  #got more cells than 1,2
  "pbmc_granulocyte_unsorted_10k"),
  num=1:4)


## Load the file created by telomemore
teloinfo <- read.csv("/corgi/cellbuster/10xpbmc/summary_kmer.java.csv")
rownames(teloinfo) <- sprintf("%s-%s",str_sub(teloinfo$barcode,1,16),map_ds2i[teloinfo$dataset,,drop=FALSE]$num)


## Transfer metadata from telomemore to adata object
teloinfo <- teloinfo[names(adata$orig.ident),]
adata$rawcnt_telo <- teloinfo$totalcnt_CCCTAA
adata$dedupcnt_telo <- teloinfo$dedupcnt_CCCTAA
adata$totalcnt_telo <- teloinfo$total
adata$norm_telo <- log10(adata$dedupcnt_telo/(adata$totalcnt_telo+1)+1e-3)
hist(adata$norm_telo)


########################################
# Rank normalized telomere counts. 
# Because library prep and sequencing affects telo abundance globally,
# this step is frequently (but not always) needed to remove batch effects
adata$rank_norm_telo <- NA
for(i in unique(adata$lib)){
  allrank <- rank(adata$norm_telo[adata$lib==i])
  adata$rank_norm_telo[adata$lib==i] <- allrank/max(allrank)
}

