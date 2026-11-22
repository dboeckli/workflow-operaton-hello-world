{{/*
Expand the name of the chart.
*/}}
{{- define "application-template.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "application-template.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "application-template.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "application-template.labels" -}}
helm.sh/chart: {{ include "application-template.chart" . }}
{{ include "application-template.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "application-template.selectorLabels" -}}
app.kubernetes.io/name: {{ include "application-template.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the FQDN for the service
*/}}
{{- define "application-template.serviceFQDN" -}}
{{- $fullname := include "application-template.fullname" . -}}
{{- printf "%s.%s.svc.cluster.local" $fullname .Release.Namespace }}
{{- end }}


{{/*
Use the apifirst-server-jpa service FQDN
*/}}
{{- define "apifirst-server-jpa.serviceFQDN" -}}
{{- printf "apifirst-server-jpa.%s.svc.cluster.local" .Release.Namespace }}
{{- end -}}


{{/*
Use the workflow-operaton-hello-world-ldap service FQDN.
The LDAP subchart's service name is its fullname (release + subchart name),
truncated to the 63-char DNS label limit.
*/}}
{{- define "application-template.ldapServiceFQDN" -}}
{{- $sub := index .Values "workflow-operaton-hello-world-ldap" | default dict -}}
{{- $name := default "workflow-operaton-hello-world-ldap" $sub.nameOverride -}}
{{- $fullname := "" -}}
{{- if $sub.fullnameOverride -}}
{{- $fullname = $sub.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else if contains $name .Release.Name -}}
{{- $fullname = .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $fullname = printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- printf "%s.%s.svc.cluster.local" $fullname .Release.Namespace -}}
{{- end -}}