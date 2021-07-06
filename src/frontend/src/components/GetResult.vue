<template>

</template>

<script>
export default {
  name: "GetResult"
  ,
  data() {
    return {
      msg: ''
    }
  },
  mounted() {
    fetch("/api/v1/result")
        .then((response) => response.text())
        .then((data) => {
          this.msg = data;
        });
  }
}
</script>

<style scoped>

</style>