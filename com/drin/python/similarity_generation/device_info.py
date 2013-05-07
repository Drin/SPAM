import pycuda.driver

pycuda.driver.init()
for device_id in range(pycuda.driver.Device.count()):
   cuda_device = pycuda.driver.Device(device_id)
   print("device: [%d: %s]" % (device_id, cuda_device.name()))
   print("compute_capability: [%s, %s]" % (cuda_device.compute_capability()))
   print("device memory: %s" % str(cuda_device.total_memory()))
