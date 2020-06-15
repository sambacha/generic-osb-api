# Generic OSB API

This project implements the OSB API and just stores and reads information about instances and bindings via a git repository. 
The actual service instances will then be deployed by a CI/CD pipeline.

This decoupling allows reusing this Generic OSB API for any kind of service that you can automatically provision from git using CI/CD.
For example you can take a look at our our [Example OSB CI Concourse Pipeline](https://github.com/meshcloud/example-osb-ci), 
which uses Terraform + Ansible to bring up new service instances.

## How to use it

You can download the [jar](https://swift.os.eu-de-darz.msh.host/swift/v1/publish/generic-osb-api-0.9.0.jar) and run the 
Generic OSB API everywhere, where Java is installed. Our recommendation is to deploy it to Cloud Foundry, which is also 
explained in one of the next sections. But you can also run it anywhere else. Only some environment variables have to 
be defined.

### Configuration

The custom configuration can be done via environment variables and the following properties can be configured:

- GIT_REMOTE: The remote Git repository to push the repo to
- GIT_LOCAL-PATH: The path where the local Git Repo shall be created/used. Defaults to tmp/git, which should be fine in
most cases.
- GIT_SSH-KEY: If you want to use SSH, please put your SSH key here. The linebreaks in the key must be replaced with 
spaces if you define it directly as an environment variable.
- GIT_USERNAME: If you use HTTPS to access the git repo, define the HTTPS username here
- GIT_PASSWORD: If you use HTTPS to access the git repo, define the HTTPS password here
- APP_BASIC-AUTH-USERNAME: The service broker API itself is secured via HTTP Basic Auth. Define the username for this here.
- APP_BASIC-AUTH-PASSWORD: Define the basic auth password for requests against the API

Additionally the catalog provided by the Service Broker has to be defined in the instances git repo. This is explained 
in the next chapter.

### Deployment to Cloud Foundry

In order to deploy the Generic OSB API to Cloud Foundry, you should use a configured Manifest file like this:

```yaml
applications:
- name: generic-osb-api
  memory: 1024M
  path: generic-osb-api-0.9.0.jar
  env:
    GIT_REMOTE: <https or ssh url for remote git repo>
    GIT_USERNAME: <if you use https, enter username here>
    GIT_PASSWORD: <if you use https, enter password here>
    GIT_SSH: <if you use ssh, enter the private SSH key here, like the example shows>
      -----BEGIN RSA PRIVATE KEY-----
      Hgiud8z89ijiojdobdikdosaa+hnjk789hdsanlklmladlsagasHOHAo7869+bcG
      x9tD2aI3ih+NJKnbikbdsaio97z9uijasnkjKJAmaölmö+eISBT8NykZuQJjcjpd
      6lTMAGod+5pIv0hWk9Us24IjTthx8K5blAACy/HsXNOH1EKSXCoqoKTehRwdXUaD
      bOclJ/U3FqswV/hjnks789za98sANoojoijoisaj/EHysKQfmAnDBdG4=
      -----END RSA PRIVATE KEY-----
    APP_BASIC-AUTH-USERNAME: <the username for securing the OSB API itself>
    APP_BASIC-AUTH-PASSWORD: <the password for securing the OSB API itself>
```

And then you can deploy it to CF:
```
cf push -f manifest.yml # deploy it to CF
```

### Run it locally

If you want to run the Generic OSB API locally, you have to add the environment variable, too. Linebreaks must be replaced
with spaces, because ENV variables are only one-liners.

```GIT_SSH-KEY=-----BEGIN RSA PRIVATE KEY----- Hgiud8z89ijiojdobdikdosaa+hnjk789hdsanlklmladlsagasHOHAo7869+bcG x9tD2aI3...ysKQfmAnDBdG4= -----END RSA PRIVATE KEY-----```

## Communication with the CI/CD pipeline

As the OSB API is completely provided by the "Generic OSB API", what you as a developer of a service broker have to focus
on is to build your CI/CD pipeline. In the following, all files that are used for communication and how the GIT repo
for exchanging information between the Generic OSB API and the pipeline are described. An example pipeline can be found
[here](https://github.com/Meshcloud/example-osb-ci).

### GIT Repo structure

```yaml
catalog.yml # file that contains all infos about services and plans this Service broker provides
instances
    <instance-id>
        instance.yml # contains all service instance info written by the Generic OSB API
        status.yml # this file contains the current status, which is updated by the pipeline
        bindings
            <binding-id>
                binding.yml # contains all binding info written by the Generic OSB API
                status.yml # this file contains the current status, which is updated by the pipeline
```

### catalog.yml

The list of provided services and their plans has to be defined in this file, which might look as follows.
```yaml
services:
  - id: "d40133dd-8373-4c25-8014-fde98f38a728"
    name: "example-osb"
    description: "This service spins up a host with OpenStack and Cloud Foundry CLI installed."
    bindable: true
    tags:
      - "example"
    plans:
      - id: "a13edcdf-eb54-44d3-8902-8f24d5acb07e"
        name: "S"
        description: "A small host with OpenStack and CloudFoundry CLI installed"
        free: true
        bindable: true
      - id: "b387b010-c002-4eab-8902-3851694ef7ba"
        name: "M"
        description: "A medium host with OpenStack and CloudFoundry CLI installed"
        free: true
        bindable: true
```

### instance.yml

The YAML structure is based on the OSB Spec (see [expected_instance.yml](src/test/resources/expected_instance.yml)).
You can use all information provided in this file in your CI/CD pipeline. The most essential properties that will be
used in all service brokers are `planId` and `deleted`. The `deleted` property is the one that indicates to the pipeline,
that this instance shall be deleted.

### binding.yml

It is basically the same as the instance.yml, but on binding level. For an example file, see
[expected_binding.yml](src/test/resources/expected_binding.yml).

### status.yml

This file contains the current status information and looks like this:
```yaml
status: "in progress"
description: "Provisioning service instance"
```

The pipeline has to update this file. When the instance or binding has been processed successfully, teh status must be 
updated to `succeeded`. In case of a failure, it must be set to `failed`. While the pipeline is still working, it might
update the description, to give the user some more information about the progress of his request. 

## Implementation

If you want to contribute to this project, or create your own service broker based on this implementation, 
you can find some details about the implementation here.

This implementation is based on Spring's [Spring Cloud Open Service Broker](https://spring.io/projects/spring-cloud-open-service-broker).
This library provides all controllers necessary to implement the OSB API. You only have to implement specific services
and can focus on the actual implementation of your Service Broker and no details of how the API must look like exactly.
The according request and response objects are provided by the library.
