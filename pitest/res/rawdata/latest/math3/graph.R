setwd("res/rawdata/latest/math3/")

##################################################
# Set working directory where AdaMu logged data
##################################################
# setwd("res/rawdata/latest/<SubjectID>")
##################################################
# $ cd <path/to/AdaMu>
# $ R --vanilla --slave < res/rawdata/latest/<SubjectID>/graph.R 
##################################################


# Read data
## runtime mutation score
rtms <- read.csv("rtms.csv")
names(rtms) <- c("elapsed.time", "score")
for(i in 0:nrow(rtms)) {
	rtms$elapsed.time[i] = rtms$elapsed.time[i] / 1000
	rtms$score[i] = rtms$score[i] * 100
}
## approximate mutation score
ams <- read.csv("ams.csv")
names(ams) <- c("elapsed.time", "mutant.order", "score")
for(i in 0:nrow(ams)) {
	ams$elapsed.time[i] = ams$elapsed.time[i] / 1000
	ams$score[i] = ams$score[i] * 100
}
## suggested approximate mutation score
suggested.ams <- read.csv("suggested.ams.csv")
names(suggested.ams) <- c("elapsed.time", "mutant.order", "score")
for(i in 0:nrow(suggested.ams)) {
	suggested.ams$elapsed.time[i] = suggested.ams$elapsed.time[i] / 1000
	suggested.ams$score[i] = suggested.ams$score[i] * 100
}

# Mean/StdDev
sink("accuracy.precision.txt")
print(paste("actual.mutation.score", rtms$score[nrow(rtms)], "0.0", sep=", "))
print(paste("runtime.mutation.score", mean(rtms$score), sd(rtms$score), sep=", "))
print(paste("suggested.approximate.mutation.score", mean(suggested.ams$score), sd(suggested.ams$score), sep=", "))
sink()

# Create plot frame
## parameters
actual.mutation.score = rtms$score[nrow(rtms)]
max.elapsed.time = ifelse(max(rtms$elapsed.time) < max(ams$elapsed.time),
	max(ams$elapsed.time), max(rtms$elapsed.time))
plot.width = 6 # 16
plot.height = 6 # 9
plot.xlim = xlim=c(0, max.elapsed.time)
plot.ylim = ylim=c(0, 100)
## plot frame
pdf("graph.pdf", width=plot.width, plot.height)
par(mar=c(4,4,1,1))
plot(0, 0, type = "n", xlim=plot.xlim, ylim=plot.ylim, xlab="", ylab="")
mtext("Elapsed time [sec]", side=1, line=2.5)
mtext("Mutation score [%]", side=2, line=2.5)
abline(h=actual.mutation.score, lty="dashed")
## plot
### runtime mutation score
lines(rtms$elapsed.time, rtms$score, lty="solid")
#points(rtms$elapsed.time, rtms$score, pch=15)
### approximate
points(suggested.ams$elapsed.time, suggested.ams$score, pch=1)
## legend
legend.label <- c(
	"RTMS",
	"AMS suggested from AdaMu",
	"Actual MS"
)
legend.lty <- c("solid", "blank", "dashed")
legend.pch <- c(46, 1, 46)
legend("bottomright", legend=legend.label, lty=legend.lty, pch=legend.pch)

# Finalize
dev.off()

