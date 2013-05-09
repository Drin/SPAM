import sys
import pstats

stats = pstats.Stats('profileOutput', stream=sys.stdout)
stats.sort_stats('cumtime', 'time')
stats.print_stats()
