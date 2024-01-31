<template>

  <div class="folder" :class="activeFolder === props.row.projectId ? 'active' : ''">
    <div class="folder__back" :style="folderBackColor">
      <div class="paper"></div>
      <div class="paper"></div>
      <div class="paper"></div>
      <div class="folder__front" :style="folderFrontColor"></div>
      <div class="folder__front right" :style="folderFrontColor" @click="openFolder(props.row)">

        <div class="row no-wrap items-center q-pa-sm rounded-borders">
          <span> {{ props.row.projectName }}</span>
          <q-space/>
          <q-btn-dropdown @show.stop="activeFolder = props.row.projectId" @before-hide.stop="activeFolder = ''" color="primary"
                          flat dropdown-icon="more_horiz" no-icon-animation padding="none" menu-anchor="top right"
                          menu-self="top left" @click.stop.prevent>
            <q-list dense>

              <FolderAction icon="edit" label="Edit" :onItemClick="() => openEditDialog(props.row)" />
              <FolderAction icon="folder_copy" label="Clone" :onItemClick="() => projectsStore.cloneProject(props.row.projectId, success)" />
              <FolderAction icon="delete" label="Delete" :onItemClick="() => projectsStore.deleteProject(props.row.projectId)" />

            </q-list>
          </q-btn-dropdown>
        </div>
        <div style="position:absolute;bottom:0;width:100%">
          <div class="row no-wrap items-ceqnter q-mt-md q-pa-sm rounded-borders">
            <q-chip :style="folderBackColor" text-color="white">
              {{ props.row.repositories.reduce((total, repo) => total + repo.datasets.length, 0) }} files
            </q-chip>
            <q-space/>
            <q-icon
              :name="optionsPrivacy.find(option => option.value === props.row.projectPrivacy).icon"
              color="primary"
            />
          </div>
        </div>
      </div>
      <div class="folder__back_after" :style="folderBackColor"></div>
    </div>
  </div>
  <CreateFolderForm v-model:show="showEditDialog" :projectData="selectedProject"/>
</template>

<script setup>
import {ref, computed} from "vue";
import {useRouter} from "vue-router";
import {colors} from 'quasar'
import {useProjectsStore} from "stores/projectsStore";
import CreateFolderForm from 'components/forms/CreateFolderForm.vue';
import {optionsPrivacy} from "./PrivacyOptions";
import FolderAction from "./FolderAction.vue";

const projectsStore = useProjectsStore()
const router = useRouter()

const showEditDialog = ref(false);
const selectedProject = ref(null);
const activeFolder = ref("")

const props = defineProps({
  row: {type: Object},
  folderColor: {type: String, default: "#3dbb94"}
});

const folderBackColor = computed(() => {
  return 'background:' + colors.lighten(props.row.projectColor, -10) + ';'
})

const folderFrontColor = computed(() => 'background:' + props.row.projectColor + ';')

const openFolder = (project) => {
  router.push({name: 'home', params: {id: project.projectId}})
  projectsStore.setCurrentProject(project)
}

const openEditDialog = (project) => {
  selectedProject.value = project;
  showEditDialog.value = true;
};

const emit = defineEmits(["submitSuccess", "cancelForm"]);
const success = () => {
  form.value.resetValidation();
  emit("submitSuccess");
};
</script>

<style lang="scss">
$folderColor: #70a1ff;
$paperColor: #ffffff;

.folder {
  transition: all 0.2s ease-in;
  margin: 10px;
  width: 100%;

  &__back {
    position: relative;
    width: 100%;
    padding: 35%;
    border-radius: 0 5px 5px 5px;

    .folder__back_after {
      position: absolute;
      bottom: 98%; 
      left: 0;
      content: "";
      width: 35%;
      height: 10%;
      border-radius: 5px 5px 0 0;
    }

    .paper {
      position: absolute;
      bottom: 10%;
      left: 50%;
      transform: translate(-50%, 10%);
      width: 70%;
      height: 80%;
      background: darken($paperColor, 10%);
      border-radius: 5px;
      transition: all 0.3s ease-in-out;

      &:nth-child(2) {
        background: darken($paperColor, 5%);
        width: 80%;
        height: 70%;
      }

      &:nth-child(3) {
        background: darken($paperColor, 0%);
        width: 90%;
        height: 60%;
      }
    }

    .folder__front {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      // background: $folderColor;
      border-radius: 5px;
      transform-origin: bottom;
      transition: all 0.3s ease-in-out;
    }
  }

  &.active {
    transform: translateY(-8px);
  }

  &.active .paper {
    transform: translate(-50%, 0%);
  }

  &.active .folder__front {
    transform: skew(15deg) scaleY(0.6);
  }

  &.active .right {
    transform: skew(-15deg) scaleY(0.6);
  }
}
</style>
