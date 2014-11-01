# Rscript --slave get_rate_ss.r --age 20 --term 10 --face 100000 --distribution male_nonsmoker

#given the cost assumptions below, age, term, face amount, and the distribution of interest, returns yearly rate for insurance

max_age=121
customer_advertizing=100 #dollars spent per new customer converted
customer_underwriting=60 #dollars spent per every customer converted
customer_aquisition=customer_advertizing+customer_underwriting #dollars spent per new customer for advertizing and underwriting
customer_maint=10 #dollars per year spent on contanct and checkups
claim_cost=2000 #dollars to process a claim
margin_pct=.30 #proportion of revenue that is gross margins
wacc =.05 #adjusted cost of capital at steady state

library(optparse)
option_list <- list(
	make_option(c("--age","-a"), type="double", default=-1, help="current decimal age of applicant"),
	make_option(c("--term","-t"), type="double", default=-1, help="decimal of term in years"),
	make_option(c("--face","-f"), type="double", default=-1, help="face value in decimal of us dollars"),
	make_option(c("--distribution","-d"), type="character", default="NA", help="a valid distribution of deaths"),
	make_option(c("--working_directory","-wd"), type="character", default="C://zenlife/", help="the working directory where the env is saved"),
	make_option(c("--source","-s"), type="character", default="life_eqns.r", help="the equations used in the calculation"),
	make_option(c("--environment","-e"), type="character", default="20141031105048.zenenv", help="the environment where the distributions have been saved")
)
opt <- parse_args(OptionParser(option_list = option_list))

if (opt$age<0 | opt$age>max_age)
{
	print(paste("please specify a valid value of --age between 0 and", max_age), stderr())
	quit(status=-1)
}
if (opt$term<0 | opt$age+opt$term>max_age)
{
	print(paste("please specify a valid value of --term greater than 0 and so that age+term<", max_age), stderr())
	quit(status=-1)
}
if (opt$face<0)
{
	print(paste("please specify a valid value of --face greater than or equal to 0", max_age), stderr())
	quit(status=-1)
}

setwd(opt$working_directory);
load(opt$environment);
#source(opt$source);

if (!exists(opt$distribution))
{
	print("please specify a valid distribution from available variables (only some of which are valid for this operation)", stderr())
	print(ls(all.names = TRUE), stderr())
	#use the dist checker to be sure
	quit(status=-1)
}

dist1=eval(parse(text=opt$distribution))
yearly_rate=hist_death_integral_ss(dist1, face=opt$face, age=opt$age, term=opt$term, grow_int=wacc, cost_int=wacc, policy_start_cost=customer_aquisition, policy_claim_cost=claim_cost, gross_margin=margin_pct, maint=customer_maint)
yearly_rate
quit()