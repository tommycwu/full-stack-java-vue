<template>
  <h1>{{ msg }}</h1>
</template>

<script>
export default {
  name: 'GetMessage',
  data() {
    return {
      msg: ''
    }
  },
  mounted() {
    fetch("/api/v1/sei")
      .then((response) => response.text())
      .then((data) => {
        this.msg = data;
      });
  }
}
</script>