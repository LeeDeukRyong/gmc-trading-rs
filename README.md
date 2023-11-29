# 프로젝트 생성 정보

* development branch 에 코드를 push 하면 CI 파이프라인 진행
* 최초 배포는 비동기로 막혀있기 때문에 배포시에는 아래 배포방법 참조
* 파이프라인은의 상태는 아래 정보 참조
    * dev
      CI: <a class="gl-mr-3" href="https://gitlab.gmc-labs.com/platform/gmc-trading-rs/-/pipelines?page=1&amp;scope=all&amp;ref=development" rel="noopener noreferrer" target="_blank"><img alt="Project badge" aria-hidden="" class="project-badge" src="https://gitlab.gmc-labs.com/platform/gmc-trading-rs/badges/development/pipeline.svg?ignore_skipped=true&amp;style=flat-square&amp;key_text=CI Dev&amp;key_width=80"></a>
    * dev
      CI: <a class="gl-mr-3" href="https://gitlab.gmc-labs.com/platform/gmc-trading-rs/-/pipelines?page=1&amp;scope=all&amp;ref=master" rel="noopener noreferrer" target="_blank"><img alt="Project badge" aria-hidden="" class="project-badge" src="https://gitlab.gmc-labs.com/platform/gmc-trading-rs/badges/master/pipeline.svg?ignore_skipped=true&amp;style=flat-square&amp;key_text=CI master&amp;key_width=80"></a>
    * Env 배포 상태
        *
        dev : <a class="gl-mr-3" href="https://argocd.gmc-labs.com/applications/gmc-trading-rs-dev?view=network" rel="noopener noreferrer" target="_blank"><img alt="Project badge" aria-hidden="" class="project-badge" src="https://argocd.gmc-labs.com/api/badge?name=gmc-trading-rs-dev&amp;revision=true"></a>
        *
        staging : <a class="gl-mr-3" href="https://argocd.gmc-labs.com/applications/gmc-trading-rs-staging?view=network" rel="noopener noreferrer" target="_blank"><img alt="Project badge" aria-hidden="" class="project-badge" src="https://argocd.gmc-labs.com/api/badge?name=gmc-trading-rs-staging&amp;revision=true"></a>
        *
        prod : <a class="gl-mr-3" href="https://argocd.gmc-labs.com/applications/gmc-trading-rs-prod?view=network" rel="noopener noreferrer" target="_blank"><img alt="Project badge" aria-hidden="" class="project-badge" src="https://argocd.gmc-labs.com/api/badge?name=gmc-trading-rs-prod&amp;revision=true"></a>
* sonarqube
    * [![Bugs](https://sonarqube.gmc-labs.com/api/project_badges/measure?project=gmc-trading-rs&metric=bugs&token=504fe970aafd0e523656e7a3aed3faa60d75f437)](https://sonarqube.gmc-labs.com/dashboard?id=gmc-trading-rs)
      [![Code Smells](https://sonarqube.gmc-labs.com/api/project_badges/measure?project=gmc-trading-rs&metric=code_smells&token=504fe970aafd0e523656e7a3aed3faa60d75f437)](https://sonarqube.gmc-labs.com/dashboard?id=gmc-trading-rs)
      [![Quality Gate Status](https://sonarqube.gmc-labs.com/api/project_badges/measure?project=gmc-trading-rs&metric=alert_status&token=504fe970aafd0e523656e7a3aed3faa60d75f437)](https://sonarqube.gmc-labs.com/dashboard?id=gmc-trading-rs)

# (권장 필수아님)신규 생성된 프로젝트 설정(API 미지원 부분만 수동으로 설정)

    ```
    gitlab.gmc-labs.com/platform/gmc-trading-rs

    Setting - General - Merge requests
    => 체크해제 : Enable 'Delete source branch' option by default
    => Squash commits when merging : Encourage
    ```

# 배포 방법

## 컨테이너 환경 설정값 수정

```
https://gitlab.gmc-labs.com/platform/helm-chart/-/tree/master/gmc-trading-rs

values-dev.yaml
values-staging.yaml
values-prod.yaml

파일의 extraEnv 값을 운영환경에 맞춰서 수정
```

