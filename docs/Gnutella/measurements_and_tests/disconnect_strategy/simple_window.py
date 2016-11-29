#!/usr/bin/env python
# encoding: utf-8

"""Check the simple congestion avoidance algorithm from TCP: 

new_value = old_value * X + current_measurement * (1-X).

Requirements: 
- pyxplot ( http://www.srcf.ucam.org/pyxplot/ )
- a unix system with a shell 
	for calling pyxplot directly from this script, 
	else you need to call pyxplot after running this script by hand via
	$ pyxplot plot_data.pyx

"""


## command line args

#from sys import argv

#if len(argv) != 3: 
	#print __doc__ + "\n\nUsage: " + argv[0] + " drop_rate param"
	#exit()

# for random connections - success/failure
from random import random

#drop_rate = float(argv[1])
#param = float(argv[2])

def calculate_conn_values(packet_stats, param): 
    """Calculate the connection quality for the given stats."""
    # the current value; initialize with 1: good connection
    current_connection_value = 1
    # all connection values over time (correspond to the random data)
    values = []
    # create all values
    for i in packet_stats: 
	    current_connection_value = param*current_connection_value + (1-param)*i
	    values.append(current_connection_value)
    return values

def write_data_files(drop_rate=0.5, param=0.8, window_size = 100, data_file="connection_stats.txt", quality_file="connection_quality.txt"): 
    """Create random data and, calculate the quality and write it to the passed files."""
    # get a list of random values, 1 is a successful packet, 0 a dropped packet
    tmp = [random()*(1.0/drop_rate) for i in range(10000)]
    rand = []
    # with window 
    sent_window = []
    drop_window = []
    for i in tmp: 
	    if i <= 1: # dropped
		    sent_window.append(1) # the sent one
		    drop_window.append(0) # the drop info
	    else: # successful
		    sent_window.append(1)
	    if len(sent_window) >= window_size: 
                    # successful / packets
                    rand.append(float(len(sent_window) - len(drop_window)) / len(sent_window))
                    sent_window = []
                    drop_window = []

    ## calculate a corresponding list of connection values

    values = calculate_conn_values(rand, param)

    # convert the data into strings
    rand = [str(i) for i in rand]
    values = [str(i) for i in values]

    # write the data, one line per value for gnuplot/pyxplot
    f = open(data_file, "w")
    f.write("\n".join(rand))
    f.close()

    f = open(quality_file, "w")
    f.write("\n".join(values))
    f.close()

# write a pyxplot plot file. 

def clear_pyxplot_file(pyxplot_file="plot_data.pyx"): 
    """Append a line to the pyxplot evaluate file."""
    f = open(pyxplot_file, "w")
    f.write("")
    f.close()

def append_pyxplot_line(line, pyxplot_file="plot_data.pyx"): 
    """Append a line to the pyxplot evaluate file."""
    f = open(pyxplot_file, "a")
    f.write("\n" + line)
    f.close()

def append_pyxplot_eval(plot_parts=["outpout.dat title 'output'"], x_label="time [packets]", y_label="quality [0:1]", title="plot", output_file="plot.png"): 
    # prepare the plot
    append_pyxplot_line("set term png")
    append_pyxplot_line('set output "' + output_file + '"')
    append_pyxplot_line('set xlabel "' + x_label + '"')
    append_pyxplot_line('set ylabel "' + y_label + '"')
    append_pyxplot_line('set title "' + title + '"')
    # create the plot line
    plot = "plot " + plot_parts[0]
    for part in plot_parts[1:]: 
	plot += ", " + part
    append_pyxplot_line(plot)
    # writeout plot
    append_pyxplot_line("replot")

def run_pyxplot(pyxplot_file="plot_data.pyx"): 
    from subprocess import call
    call("pyxplot " + pyxplot_file, shell=True)

def do_and_evaluate_tests(): 
    """Do a set of different tests and evaluate them via pyxplot."""
    # First clear the pyxplot file
    clear_pyxplot_file()

    # Then prepare data files and corresponding pyxplot eval lines
    for drop_rate in [0.1, 0.3, 0.6, 0.9]: 
	for param in [0.9, 0.995]: 

	    write_data_files(drop_rate=drop_rate, param=param, data_file="connection_stats-" + str(drop_rate) + "-" + str(param) + ".txt", quality_file="connection_quality-" + str(drop_rate) + "-" + str(param) + ".txt")
	    
	    append_pyxplot_eval(plot_parts=['"connection_stats-' + str(drop_rate) + "-" + str(param) + '.txt" title "packets"', '"connection_quality-' + str(drop_rate) + "-" + str(param) + '.txt" title "quality" with lines'], x_label="time [packets]", y_label="quality [0:1]", title="plot with drop rate: " + str(drop_rate) + " and param: " + str(param), output_file="plot-" + str(drop_rate) + "-" + str(param) + ".png")
    
    # finally run pyxplot
    run_pyxplot()

if __name__ == "__main__": 
    do_and_evaluate_tests()
